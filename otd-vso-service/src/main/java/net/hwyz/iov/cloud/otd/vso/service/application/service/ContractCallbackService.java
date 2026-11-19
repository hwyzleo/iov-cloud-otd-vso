package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ContractCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.CallbackLogRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderStatusDimensionRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderStatusDimensionPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 电子签回调应用服务（OTD-VSO-DSN-CR-014 US-032 / INV-042 / INV-046）
 * <p>
 * 签名、防重放、签署任务匹配、幂等与状态机校验通过后，同事务写：
 * 合同事实 + 合同状态投影 + 回调日志 + 时间线；冲突回调不回退合同、不追加成功时间线。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractCallbackService {

    static final String BUSINESS_TYPE_CONTRACT = "CONTRACT";
    static final String EXTERNAL_SYSTEM_MOCK_ESIGN = "MOCK_ESIGN";

    private final ContractRepository contractRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusDimensionRepository orderStatusDimensionRepository;
    private final CallbackLogRepository callbackLogRepository;
    private final OrderTimelineAppService orderTimelineAppService;
    private final CallbackSignatureService callbackSignatureService;
    private final ObjectMapper objectMapper;
    private final BusinessTimeZone businessTimeZone;

    /**
     * 处理电子签回调
     *
     * @param cmd       回调命令
     * @param signature X-Signature 原文
     * @param timestamp 回调时间戳（Unix 秒）
     * @param nonce     随机串
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentCallbackResult handleCallback(ContractCallbackCmd cmd, String signature,
                                                Long timestamp, String nonce) {
        log.info("处理电子签回调：contractNo={}, signNo={}, status={}",
                cmd == null ? null : cmd.getContractNo(),
                cmd == null ? null : cmd.getSignNo(),
                cmd == null || cmd.getStatus() == null ? null : cmd.getStatus());

        if (cmd == null || cmd.getStatus() == null) {
            saveCallbackLog(null, cmd, null, "FAIL", "不支持的合同回调状态");
            return result(PaymentCallbackResultCode.FAIL, "不支持的合同回调状态");
        }
        if (cmd.getIdempotentKey() == null || cmd.getIdempotentKey().isBlank()) {
            saveCallbackLog(null, cmd, null, "FAIL", "幂等键不能为空");
            return result(PaymentCallbackResultCode.FAIL, "幂等键不能为空");
        }
        if (!verifySignature(cmd, signature, timestamp, nonce)) {
            saveCallbackLog(null, cmd, cmd.getIdempotentKey(), "FAIL", "签名或防重放校验失败");
            return result(PaymentCallbackResultCode.FAIL, "签名或防重放校验失败");
        }

        Contract contract = contractRepository.findBySignNo(cmd.getSignNo()).orElse(null);
        if (contract == null || !cmd.getContractNo().equals(contract.getContractNo())
                || !cmd.getSignNo().equals(contract.getSignNo())) {
            saveCallbackLog(contract == null ? null : contract.getOrderId(), cmd, cmd.getIdempotentKey(),
                    "FAIL", "合同或签署任务不存在/不匹配");
            return result(PaymentCallbackResultCode.FAIL, "合同或签署任务不存在/不匹配");
        }

        switch (cmd.getStatus()) {
            case SIGNING -> {
                if (contract.getContractStatus() == ContractStatus.SIGNING) {
                    saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "SUCCESS", null);
                    return result(PaymentCallbackResultCode.DUPLICATE, "该回调已处理");
                }
                return conflict(contract, cmd, "合同当前状态[" + contract.getContractStatus() + "]不接受签署中回调");
            }
            case SIGNED -> {
                if (contract.isSignTerminal()) {
                    saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "SUCCESS", null);
                    return result(PaymentCallbackResultCode.DUPLICATE, "该回调已处理");
                }
                if (contract.getContractStatus() != ContractStatus.SIGNING) {
                    return conflict(contract, cmd, "合同当前状态[" + contract.getContractStatus() + "]不允许签署成功");
                }
                contract.markSigned(eventTime(cmd));
                persistSigned(contract, cmd);
                orderTimelineAppService.record(contract.getOrderId(), OrderTimelineEventType.CONTRACT_SIGNED,
                        orderStateOf(contract), orderStateOf(contract),
                        "system", "system", "esign_callback", contract.getSignNo(), null,
                        "购车合同已签署：" + contract.getContractNo());
                saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "SUCCESS", null);
                return result(PaymentCallbackResultCode.SUCCESS, "回调处理成功");
            }
            case EFFECTIVE -> {
                if (contract.getContractStatus() == ContractStatus.EFFECTIVE) {
                    saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "SUCCESS", null);
                    return result(PaymentCallbackResultCode.DUPLICATE, "该回调已处理");
                }
                if (contract.getContractStatus() != ContractStatus.SIGNED) {
                    return conflict(contract, cmd, "合同当前状态[" + contract.getContractStatus() + "]不允许生效");
                }
                contract.markEffective(eventTime(cmd));
                persistSigned(contract, cmd);
                saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "SUCCESS", null);
                return result(PaymentCallbackResultCode.SUCCESS, "回调处理成功");
            }
            case INVALID -> {
                if (contract.getContractStatus() == ContractStatus.INVALID) {
                    saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "SUCCESS", null);
                    return result(PaymentCallbackResultCode.DUPLICATE, "该回调已处理");
                }
                if (contract.getContractStatus() != ContractStatus.SIGNING) {
                    return conflict(contract, cmd, "合同当前状态[" + contract.getContractStatus() + "]不允许失效");
                }
                contract.markInvalid(eventTime(cmd), cmd.getFailReason());
                persistSigned(contract, cmd);
                orderTimelineAppService.record(contract.getOrderId(), OrderTimelineEventType.CONTRACT_INVALID,
                        orderStateOf(contract), orderStateOf(contract),
                        "system", "system", "esign_callback", contract.getSignNo(), cmd.getFailReason(),
                        "购车合同已失效：" + contract.getContractNo());
                saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "SUCCESS", null);
                return result(PaymentCallbackResultCode.SUCCESS, "回调处理成功");
            }
            default -> {
                saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "FAIL", "不支持的合同回调状态");
                return result(PaymentCallbackResultCode.FAIL, "不支持的合同回调状态");
            }
        }
    }

    /**
     * 保存合同事实并同步合同状态投影（同一事务）
     */
    private void persistSigned(Contract contract, ContractCallbackCmd cmd) {
        contractRepository.save(contract);
        syncContractStatusProjection(contract);
    }

    private void syncContractStatusProjection(Contract contract) {
        OrderStatusDimensionPo projection = orderStatusDimensionRepository.findByOrderId(contract.getOrderId())
                .orElseGet(() -> {
                    OrderStatusDimensionPo po = new OrderStatusDimensionPo();
                    po.setStatusDimensionId(IdUtil.nanoId(15));
                    po.setOrderId(contract.getOrderId());
                    return po;
                });
        projection.setContractStatus(contract.getContractStatus().name());
        projection.setLastSyncSource(EXTERNAL_SYSTEM_MOCK_ESIGN);
        projection.setLastSyncTime(LocalDateTime.now());
        orderStatusDimensionRepository.save(projection);
    }

    private PaymentCallbackResult conflict(Contract contract, ContractCallbackCmd cmd, String reason) {
        saveCallbackLog(contract.getOrderId(), cmd, cmd.getIdempotentKey(), "FAIL", reason);
        return result(PaymentCallbackResultCode.CONFLICT, reason);
    }

    /**
     * 回调状态与命令状态一致的幂等键（签名内容：contractNo、signNo、status、occurredAt）
     */
    private boolean verifySignature(ContractCallbackCmd cmd, String signature, Long timestamp, String nonce) {
        Map<String, String> signData = new HashMap<>();
        signData.put("contractNo", nullSafe(cmd.getContractNo()));
        signData.put("signNo", nullSafe(cmd.getSignNo()));
        signData.put("status", cmd.getStatus().name());
        signData.put("occurredAt", cmd.getOccurredAt() == null ? "" : cmd.getOccurredAt().toString());
        return callbackSignatureService.verifySignature(signature, timestamp, nonce, signData);
    }

    private LocalDateTime eventTime(ContractCallbackCmd cmd) {
        return businessTimeZone.toLocalDateTime(cmd.getOccurredAt() == null
                ? java.time.Instant.now() : cmd.getOccurredAt());
    }

    private String orderStateOf(Contract contract) {
        return orderRepository.findByOrderId(contract.getOrderId())
                .map(Order::getOrderState)
                .map(state -> String.valueOf(state.getValue()))
                .orElse(null);
    }

    private void saveCallbackLog(String orderId, ContractCallbackCmd cmd, String idempotentKey,
                                 String processResult, String failReason) {
        CallbackLogPo logPo = new CallbackLogPo();
        logPo.setCallbackLogId(IdUtil.nanoId(15));
        logPo.setOrderId(orderId);
        logPo.setBusinessType(BUSINESS_TYPE_CONTRACT);
        logPo.setExternalSystemName(EXTERNAL_SYSTEM_MOCK_ESIGN);
        logPo.setExternalBusinessNo(cmd == null ? null : cmd.getSignNo());
        logPo.setIdempotentKey(idempotentKey);
        logPo.setCallbackStatusValue(cmd == null || cmd.getStatus() == null ? null : cmd.getStatus().name());
        logPo.setCallbackResultCode(cmd == null || cmd.getStatus() == null ? null : cmd.getStatus().name());
        logPo.setEventTime(cmd == null ? null : eventTime(cmd));
        logPo.setRequestBody(serialize(cmd));
        logPo.setProcessResult(processResult);
        logPo.setFailReason(failReason);
        logPo.setProcessTime(LocalDateTime.now());
        logPo.setManualOverrideFlag(0);
        callbackLogRepository.save(logPo);
    }

    private String serialize(ContractCallbackCmd cmd) {
        if (cmd == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(cmd);
        } catch (JsonProcessingException e) {
            log.warn("序列化电子签回调请求失败", e);
            return "{}";
        }
    }

    private PaymentCallbackResult result(PaymentCallbackResultCode code, String message) {
        return PaymentCallbackResult.builder().code(code).message(message).build();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
