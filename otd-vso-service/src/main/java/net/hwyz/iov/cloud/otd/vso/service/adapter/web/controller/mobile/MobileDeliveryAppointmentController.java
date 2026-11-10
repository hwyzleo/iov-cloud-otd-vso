package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request.CreateDeliveryAppointmentRequest;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request.ModifyDeliveryAppointmentRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryAppointmentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.DeliveryAppService;
import org.springframework.web.bind.annotation.*;

/**
 * C 端交付预约接口（OTD-VSO-DSN-CR-002 US-009）
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mobile/vso/v1/deliveryAppointment")
public class MobileDeliveryAppointmentController extends BaseController {

    private final DeliveryAppService deliveryAppService;

    @PostMapping("/action/create")
    public ApiResponse<DeliveryAppointmentResult> create(@RequestBody @Valid CreateDeliveryAppointmentRequest request) {
        return ApiResponse.ok(deliveryAppService.create(SecurityContextHolder.getUserId(),
                request.getOrderNo(), request.getAppointmentTime(),
                request.getAppointmentStoreCode(), request.getAppointmentPlace()));
    }

    @PostMapping("/action/modify")
    public ApiResponse<DeliveryAppointmentResult> modify(@RequestBody @Valid ModifyDeliveryAppointmentRequest request) {
        return ApiResponse.ok(deliveryAppService.modify(SecurityContextHolder.getUserId(),
                request.getAppointmentNo(), request.getAppointmentTime(),
                request.getAppointmentStoreCode(), request.getAppointmentPlace(), request.getReason()));
    }

    @PostMapping("/action/cancel")
    public ApiResponse<DeliveryAppointmentResult> cancel(@RequestBody @Valid ModifyDeliveryAppointmentRequest request) {
        return ApiResponse.ok(deliveryAppService.cancel(SecurityContextHolder.getUserId(),
                request.getAppointmentNo(), request.getReason()));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<DeliveryAppointmentResult> get(@PathVariable String orderNo) {
        return ApiResponse.ok(deliveryAppService.getByOrderNo(SecurityContextHolder.getUserId(), orderNo));
    }
}
