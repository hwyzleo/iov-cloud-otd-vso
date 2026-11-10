package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 交付预约冲突异常
 *
 * @author VSO Team
 */
public class DeliveryAppointmentConflictException extends VsoBaseException {

    public DeliveryAppointmentConflictException(String message) {
        super(VsoErrorCode.DELIVERY_APPOINTMENT_CONFLICT, message);
    }
}
