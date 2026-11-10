package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 车辆交付记录领域对象
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRecord {

    private String deliveryRecordId;

    private String orderId;

    private String deliveryAppointmentId;

    private String deliveryStatus;

    private String deliveryMode;

    private LocalDateTime actualDeliveryTime;

    private String deliveryPlace;

    private String deliveryStoreCode;

    private String vehicleVin;

    private String deliveryRemark;
}
