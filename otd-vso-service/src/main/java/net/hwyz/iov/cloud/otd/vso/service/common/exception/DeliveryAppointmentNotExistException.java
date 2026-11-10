package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 交付预约不存在异常
 *
 * @author VSO Team
 */
public class DeliveryAppointmentNotExistException extends VsoBaseException {

    public DeliveryAppointmentNotExistException(String message) {
        super(VsoErrorCode.DELIVERY_APPOINTMENT_NOT_EXIST, message);
    }
}
