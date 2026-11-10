package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交付预约与交付记录结果
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAppointmentResult {

    private String appointmentNo;
    private String orderNo;
    private String appointmentStatus;
    private String deliveryMode;
    private LocalDateTime appointmentTime;
    private String appointmentStoreCode;
    private String appointmentPlace;
    private String deliveryPersonId;
    private String deliveryPersonName;
    private String vehicleVin;
    private Boolean delivered;
    private List<RecordItem> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordItem {
        private String deliveryRecordId;
        private String deliveryStatus;
        private LocalDateTime actualDeliveryTime;
        private String deliveryPlace;
        private String vehicleVin;
    }
}
