package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request.SavePurchaseAgreementRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.SavePurchaseAgreementCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PurchaseAgreementResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PurchaseAgreementAppService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购车协议后台管理（MPT）
 * <p>
 * 用户明确要求：完整功能必须包含后台管理（对 OTD-VSO-DSN-CR-002 §7.2「本期不新增协议运营 CRUD」的扩展）。
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mpt/vso/v1/purchaseAgreement")
public class MptPurchaseAgreementController extends BaseController {

    private final PurchaseAgreementAppService purchaseAgreementAppService;

    /**
     * 协议模板列表
     */
    @RequiresPermissions("completeVehicle:order:agreement:list")
    @GetMapping("/list")
    public ApiResponse<PageResult<PurchaseAgreementResult>> list(
            @RequestParam(required = false) String saleModelCode,
            @RequestParam(required = false) String agreementCode,
            @RequestParam(required = false) String agreementStatus) {
        startPage();
        List<PurchaseAgreementResult> result = purchaseAgreementAppService.search(
                saleModelCode, agreementCode, agreementStatus);
        return ApiResponse.ok(getPageResult(result));
    }

    /**
     * 新增或修改协议模板
     */
    @RequiresPermissions("completeVehicle:order:agreement:edit")
    @PostMapping("/action/save")
    public ApiResponse<String> save(@RequestBody @Valid SavePurchaseAgreementRequest request) {
        SavePurchaseAgreementCmd cmd = SavePurchaseAgreementCmd.builder()
                .agreementId(request.getAgreementId())
                .agreementCode(request.getAgreementCode())
                .saleModelCode(request.getSaleModelCode())
                .versionNo(request.getVersionNo())
                .title(request.getTitle())
                .intro(request.getIntro())
                .detail(request.getDetail())
                .agreementStatus(request.getAgreementStatus())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();
        return ApiResponse.ok(purchaseAgreementAppService.save(cmd));
    }

    /**
     * 下架协议模板
     */
    @RequiresPermissions("completeVehicle:order:agreement:remove")
    @PostMapping("/action/offShelf")
    public ApiResponse<Void> offShelf(@RequestParam String agreementId) {
        purchaseAgreementAppService.offShelf(agreementId);
        return ApiResponse.ok();
    }
}
