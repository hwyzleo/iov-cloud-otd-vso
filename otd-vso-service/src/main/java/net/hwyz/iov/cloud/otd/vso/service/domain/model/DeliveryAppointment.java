package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DeliveryAppointmentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DeliveryAppointmentStatusNotAllowedException;

import java.time.LocalDateTime;

/**
 * 交付预约领域对象
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAppointment {

    /** 交付中心交付方式 */
    public static final String MODE_DELIVERY_CENTER = "delivery_center";

    private String appointmentId;

    private String appointmentNo;

    private String orderId;

    private DeliveryAppointmentStatus appointmentStatus;

    private String deliveryMode;

    private LocalDateTime appointmentTime;

    private String appointmentPlace;

    private String appointmentStoreCode;

    private String rescheduleReason;

    private String cancelReason;

    private String operatorId;

    /**
     * 修改预约：仅本人 booked 可改期
     */
    public void reschedule(LocalDateTime appointmentTime, String storeCode, String place, String reason) {
        if (appointmentStatus != DeliveryAppointmentStatus.BOOKED) {
            throw new DeliveryAppointmentStatusNotAllowedException(
                    "预约[" + appointmentNo + "]当前状态[" + appointmentStatus + "]不允许修改");
        }
        this.appointmentTime = appointmentTime;
        if (storeCode != null && !storeCode.isEmpty()) {
            this.appointmentStoreCode = storeCode;
        }
        if (place != null && !place.isEmpty()) {
            this.appointmentPlace = place;
        }
        this.rescheduleReason = reason;
    }

    /**
     * 取消预约：仅 booked 可取消
     */
    public void cancel(String reason) {
        if (appointmentStatus != DeliveryAppointmentStatus.BOOKED) {
            throw new DeliveryAppointmentStatusNotAllowedException(
                    "预约[" + appointmentNo + "]当前状态[" + appointmentStatus + "]不允许取消");
        }
        this.appointmentStatus = DeliveryAppointmentStatus.CANCELLED;
        this.cancelReason = reason;
    }

    /**
     * 交付完成：booked → completed
     */
    public void complete() {
        if (appointmentStatus == DeliveryAppointmentStatus.COMPLETED) {
            return;
        }
        if (appointmentStatus != DeliveryAppointmentStatus.BOOKED) {
            throw new DeliveryAppointmentConflictException(
                    "预约[" + appointmentNo + "]当前状态[" + appointmentStatus + "]不允许完成交付");
        }
        this.appointmentStatus = DeliveryAppointmentStatus.COMPLETED;
    }
}
