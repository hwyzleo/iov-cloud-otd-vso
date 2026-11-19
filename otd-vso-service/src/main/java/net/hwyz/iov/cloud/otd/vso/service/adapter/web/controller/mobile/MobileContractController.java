package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request.InitiateContractSignRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiateContractSignCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractSignResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.ContractAppService;
import org.springframework.web.bind.annotation.*;

/**
 * C 端购车合同接口（OTD-VSO-DSN-CR-014 US-032）
 * <p>
 * 账号只取安全上下文；Controller 只组装命令并委托 ContractAppService，
 * 不直接调用 Adapter/Repository；不提供客户端直接把合同置为 SIGNED/EFFECTIVE 的接口。
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mobile/vso/v1")
public class MobileContractController extends BaseController {

    private final ContractAppService contractAppService;

    /**
     * 发起合同签署（本人、600、全款订单）
     */
    @PostMapping("/order/action/initiateContractSign")
    public ApiResponse<ContractSignResult> initiateContractSign(
            @Valid @RequestBody InitiateContractSignRequest request) {
        log.info("手机客户端[{}]发起购车合同签署：orderNo={}",
                ParamHelper.getClientAccountInfo(), request.getOrderNo());
        ContractSignResult result = contractAppService.initiateSign(InitiateContractSignCmd.builder()
                .accountId(SecurityContextHolder.getUserId())
                .orderNo(request.getOrderNo())
                .build());
        return ApiResponse.ok(result);
    }

    /**
     * 查询当前购车合同
     */
    @GetMapping("/order/{orderNo}/contract")
    public ApiResponse<ContractDetailResult> getContract(@PathVariable String orderNo) {
        log.info("手机客户端[{}]查询购车合同：orderNo={}", ParamHelper.getClientAccountInfo(), orderNo);
        return ApiResponse.ok(contractAppService.getContract(SecurityContextHolder.getUserId(), orderNo));
    }
}
