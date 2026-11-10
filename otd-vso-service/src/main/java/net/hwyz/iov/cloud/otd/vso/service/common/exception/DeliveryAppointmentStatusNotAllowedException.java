package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 交付预约状态不允许异常
 *
 * @author VSO Team
 */
public class DeliveryAppointmentStatusNotAllowedException extends VsoBaseException {

    public DeliveryAppointmentStatusNotAllowedException(String message) {
        super(VsoErrorCode.DELIVERY_APPOINTMENT_STATUS_NOT_ALLOWED, message);
    }
}
