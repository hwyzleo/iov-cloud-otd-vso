package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建交付预约请求
 *
 * @author VSO Team
 */
@Data
public class CreateDeliveryAppointmentRequest {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;

    @NotBlank(message = "预约门店编码不能为空")
    private String appointmentStoreCode;

    private String appointmentPlace;
}
