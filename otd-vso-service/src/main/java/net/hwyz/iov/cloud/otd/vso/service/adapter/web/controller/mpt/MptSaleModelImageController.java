package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageCompleteDto;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageResultVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageTicketDto;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageTicketVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GenerateSaleModelImageUploadCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelImageAppService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MPT 销售车型图片上传薄封装接口（OTD-VSO-DSN-CR-003 §4）
 * <p>
 * 仅 MPT 路由暴露，Mobile 不注册同路径；不做业务判断、不直连 Feign、不吞异常。
 * 操作人一律取安全上下文，不接受 X-Operator-Id 请求头。
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mpt/saleModel/v1/upload")
public class MptSaleModelImageController extends BaseController {

    private final SaleModelImageAppService saleModelImageAppService;

    /**
     * 申请上传凭证（US-013）
     */
    @PostMapping("/tickets")
    public ApiResponse<SaleModelImageTicketVo> createTicket(@Valid @RequestBody SaleModelImageTicketDto dto) {
        String operatorId = SecurityContextHolder.getUserId();
        log.info("管理后台用户[{}]申请销售车型图片上传凭证，文件[{}] MIME[{}] 大小[{}]",
                operatorId, dto.getOriginalFilename(), dto.getContentType(), dto.getSize());
        return ApiResponse.ok(saleModelImageAppService.generateTicket(GenerateSaleModelImageUploadCmd.builder()
                .originalFilename(dto.getOriginalFilename())
                .contentType(dto.getContentType())
                .size(dto.getSize())
                .operatorId(operatorId)
                .build()));
    }

    /**
     * 完成确认（US-013）
     */
    @PostMapping("/tickets/{objectId}/complete")
    public ApiResponse<SaleModelImageResultVo> completeTicket(@PathVariable String objectId,
                                                              @Valid @RequestBody SaleModelImageCompleteDto dto) {
        String operatorId = SecurityContextHolder.getUserId();
        log.info("管理后台用户[{}]确认销售车型图片上传，对象[{}]", operatorId, objectId);
        return ApiResponse.ok(saleModelImageAppService.complete(objectId, dto.getUploadToken(), operatorId));
    }
}
