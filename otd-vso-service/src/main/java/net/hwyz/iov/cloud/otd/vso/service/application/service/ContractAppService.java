package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiateContractSignCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractSignResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ContractNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ContractStatusNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.EsignGatewayFailedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.TailPaymentNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.ContractGenerationResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.EsignAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.EsignSignStatusResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.SignTaskResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 购车合同应用服务（OTD-VSO-DSN-CR-014 US-032）
 * <p>
 * 合同生成/签署分阶段持久化，外部调用不包在数据库事务中：
 * TX1 建 DRAFT → 外部生成 → TX2 保存 GENERATED/fileUrl/externalContractNo →
 * 外部创建签署任务 → TX3 保存 SIGNING/signNo → 提交后 Mock 自回调。
 * 每个阶段可恢复，重复请求从当前状态继续；签署终态只允许回调路径产生。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAppService {

    private final OrderRepository orderRepository;
    private final ContractRepository contractRepository;
    private final EsignAdapter esignAdapter;
    private final OrderLockService orderLockService;
    private final OrderTimelineAppService orderTimelineAppService;
    private final MockContractAutoCallbackService mockContractAutoCallbackService;
    private final PlatformTransactionManager transactionManager;
    private final BusinessTimeZone businessTimeZone;

    /**
     * 发起合同签署（本人订单、600、全款）
     */
    public ContractSignResult initiateSign(InitiateContractSignCmd cmd) {
        if (cmd.getOrderNo() == null || cmd.getOrderNo().isBlank()) {
            throw new OrderNotExistException(String.valueOf(cmd.getOrderNo()));
        }
        return orderLockService.executeWithLock(cmd.getOrderNo(), cmd.getAccountId(), "contract",
                () -> doInitiateSign(cmd));
    }

    /**
     * 查询当前购车合同（本人订单）
     */
    public ContractDetailResult getContract(String accountId, String orderNo) {
        Order order = orderRepository.findByOrderNoAndAccountId(orderNo, accountId)
                .orElseThrow(() -> new OrderNotExistException(orderNo));
        Contract contract = contractRepository.findByOrderIdAndType(order.getId(), Contract.TYPE_VEHICLE_PURCHASE)
                .orElseThrow(() -> new ContractNotExistException(orderNo));

        String signUrl = null;
        if (contract.getContractStatus() == ContractStatus.SIGNING && contract.getSignNo() != null) {
            signUrl = querySignUrl(contract.getSignNo());
        }
        return toDetailResult(contract, signUrl);
    }

    private ContractSignResult doInitiateSign(InitiateContractSignCmd cmd) {
        Order order = orderRepository.findByOrderNoAndAccountId(cmd.getOrderNo(), cmd.getAccountId())
                .orElseThrow(() -> new OrderNotExistException(cmd.getOrderNo()));
        validateOrder(order);

        Contract contract = contractRepository.findByOrderIdAndType(order.getId(), Contract.TYPE_VEHICLE_PURCHASE)
                .orElse(null);

        if (contract == null) {
            contract = inTransaction(() -> contractRepository.save(
                    Contract.draft(IdUtil.nanoId(15), order.getId(), "CT" + IdUtil.nanoId(12))));
            log.info("购车合同草稿已创建：orderNo={}, contractNo={}", order.getOrderNo(), contract.getContractNo());
        }

        // 签署终态：幂等返回当前合同事实，不新建合同或签署任务
        if (contract.isSignTerminal()) {
            log.info("购车合同已签署/生效，幂等返回：orderNo={}, contractNo={}, status={}",
                    order.getOrderNo(), contract.getContractNo(), contract.getContractStatus());
            return toSignResult(contract, null);
        }

        // SIGNING 且已有有效 signNo：复用当前签署任务，不重复创建
        if (contract.getContractStatus() == ContractStatus.SIGNING && contract.getSignNo() != null) {
            log.info("购车合同签署中，复用当前签署任务：orderNo={}, signNo={}",
                    order.getOrderNo(), contract.getSignNo());
            return toSignResult(contract, querySignUrl(contract.getSignNo()));
        }

        if (contract.getContractStatus() == ContractStatus.DRAFT) {
            contract = generate(contract, order, cmd.getAccountId());
        }

        SignTaskResult task = callCreateSignTask(contract, order, cmd.getAccountId());
        Contract signing = persistSigning(contract, order, cmd.getAccountId(), task);
        maybeAutoCallback(signing.getContractNo());
        // 自回调可能已将合同推进到终态，返回当前事实而非旧快照
        Contract current = contractRepository
                .findByOrderIdAndType(order.getId(), Contract.TYPE_VEHICLE_PURCHASE)
                .orElse(signing);
        return toSignResult(current, current.getContractStatus() == ContractStatus.SIGNING
                ? task.getSignUrl() : null);
    }

    private Contract generate(Contract contract, Order order, String accountId) {
        ContractGenerationResult generation;
        try {
            generation = esignAdapter.generateContract(order.getId(), contract.getContractType(), null);
        } catch (Exception e) {
            log.error("电子签生成合同失败：orderNo={}, contractNo={}", order.getOrderNo(), contract.getContractNo(), e);
            throw new EsignGatewayFailedException("电子签生成合同失败，请稍后重试");
        }
        if (generation == null || isBlank(generation.getExternalContractNo()) || isBlank(generation.getFileUrl())) {
            throw new EsignGatewayFailedException("电子签生成合同返回结果不完整");
        }

        Contract generated = inTransaction(() -> {
            Contract current = contractRepository
                    .findByOrderIdAndType(order.getId(), Contract.TYPE_VEHICLE_PURCHASE)
                    .orElseThrow(() -> new ContractNotExistException(order.getOrderNo()));
            current.markGenerated(generation.getExternalContractNo(), generation.getFileUrl(),
                    generation.getGenerateTime());
            return contractRepository.save(current);
        });
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.CONTRACT_GENERATED,
                String.valueOf(order.getOrderState().getValue()), String.valueOf(order.getOrderState().getValue()),
                accountId, "order_user", "capp", generated.getContractNo(), null,
                "生成购车合同：" + generated.getContractNo());
        log.info("购车合同已生成：orderNo={}, contractNo={}", order.getOrderNo(), generated.getContractNo());
        return generated;
    }

    private SignTaskResult callCreateSignTask(Contract contract, Order order, String accountId) {
        SignTaskResult task;
        try {
            task = esignAdapter.createSignTask(contract.getContractId(), accountId);
        } catch (Exception e) {
            log.error("电子签创建签署任务失败：orderNo={}, contractNo={}", order.getOrderNo(), contract.getContractNo(), e);
            throw new EsignGatewayFailedException("电子签创建签署任务失败，请稍后重试");
        }
        if (task == null || isBlank(task.getSignNo())) {
            throw new EsignGatewayFailedException("电子签创建签署任务返回结果不完整");
        }
        return task;
    }

    private Contract persistSigning(Contract contract, Order order, String accountId, SignTaskResult task) {
        Contract signing = inTransaction(() -> {
            Contract current = contractRepository
                    .findByOrderIdAndType(order.getId(), Contract.TYPE_VEHICLE_PURCHASE)
                    .orElseThrow(() -> new ContractNotExistException(order.getOrderNo()));
            if (current.isSignTerminal()) {
                // 外部调用期间回调已到达：保持终态，不回退
                return current;
            }
            current.markSigning(task.getSignNo());
            return contractRepository.save(current);
        });
        if (signing.getContractStatus() == ContractStatus.SIGNING && task.getSignNo().equals(signing.getSignNo())) {
            orderTimelineAppService.record(order.getId(), OrderTimelineEventType.CONTRACT_SIGNING,
                    String.valueOf(order.getOrderState().getValue()), String.valueOf(order.getOrderState().getValue()),
                    accountId, "order_user", "capp", signing.getSignNo(), null,
                    "发起购车合同签署：" + signing.getContractNo());
        }
        log.info("购车合同签署任务已创建：orderNo={}, contractNo={}, signNo={}",
                order.getOrderNo(), signing.getContractNo(), signing.getSignNo());
        return signing;
    }

    private void maybeAutoCallback(String contractNo) {
        try {
            mockContractAutoCallbackService.triggerIfEnabled(contractNo);
        } catch (Exception e) {
            // Mock 自回调失败仅告警：合同停留 SIGNING，等待合法外部回调
            log.warn("Mock 电子签自动回调失败（不影响发起签署）：contractNo={}", contractNo, e);
        }
    }

    private String querySignUrl(String signNo) {
        try {
            EsignSignStatusResult status = esignAdapter.querySignStatus(signNo);
            return status == null ? null : status.getSignUrl();
        } catch (Exception e) {
            log.error("电子签查询签署状态失败：signNo={}", signNo, e);
            throw new EsignGatewayFailedException("电子签查询签署状态失败，请稍后重试");
        }
    }

    private void validateOrder(Order order) {
        if (order.getOrderState() != OrderState.PREPARE_DELIVER) {
            throw new ContractStatusNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]当前状态["
                    + order.getOrderState() + "]不允许发起合同签署，仅 PREPARE_DELIVER 可签署");
        }
        if (!"full_payment".equals(order.getPaymentMethod())) {
            throw new TailPaymentNotAllowedException("车辆销售订单[" + order.getOrderNo() + "]非全款订单，不允许发起购车合同签署");
        }
    }

    private ContractSignResult toSignResult(Contract contract, String signUrl) {
        return ContractSignResult.builder()
                .contractNo(contract.getContractNo())
                .contractType(contract.getContractType())
                .contractStatus(contract.getContractStatus() == null ? null : contract.getContractStatus().name())
                .signNo(contract.getSignNo())
                .signUrl(signUrl)
                .fileUrl(contract.getFileUrl())
                .generateTime(businessTimeZone.toInstant(contract.getGenerateTime()))
                .build();
    }

    private ContractDetailResult toDetailResult(Contract contract, String signUrl) {
        ContractStatus status = contract.getContractStatus();
        boolean signUrlVisible = status == ContractStatus.SIGNING;
        return ContractDetailResult.builder()
                .contractNo(contract.getContractNo())
                .contractType(contract.getContractType())
                .contractStatus(status == null ? null : status.name())
                .signNo(contract.getSignNo())
                .signUrl(signUrlVisible ? signUrl : null)
                .fileUrl(contract.getFileUrl())
                .generateTime(businessTimeZone.toInstant(contract.getGenerateTime()))
                .signTime(businessTimeZone.toInstant(contract.getSignTime()))
                .effectiveTime(businessTimeZone.toInstant(contract.getEffectiveTime()))
                .invalidTime(businessTimeZone.toInstant(contract.getInvalidTime()))
                .invalidReason(contract.getInvalidReason())
                .build();
    }

    private <T> T inTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> supplier.get());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
