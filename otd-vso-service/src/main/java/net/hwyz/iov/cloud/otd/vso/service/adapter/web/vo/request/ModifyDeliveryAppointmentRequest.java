package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改/取消交付预约请求
 *
 * @author VSO Team
 */
@Data
public class ModifyDeliveryAppointmentRequest {

    @NotBlank(message = "预约单号不能为空")
    private String appointmentNo;

    private LocalDateTime appointmentTime;

    private String appointmentStoreCode;

    private String appointmentPlace;

    private String reason;
}
