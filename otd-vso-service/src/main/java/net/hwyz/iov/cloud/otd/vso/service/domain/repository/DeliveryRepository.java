package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;

import java.util.List;
import java.util.Optional;

/**
 * 交付仓储接口（领域类型）
 *
 * @author VSO Team
 */
public interface DeliveryRepository {

    DeliveryAppointment saveAppointment(DeliveryAppointment appointment);

    /**
     * 根据预约单号查询（具名 SQL）
     */
    Optional<DeliveryAppointment> findByAppointmentNo(String appointmentNo);

    /**
     * 查询订单当前有效（booked）预约
     */
    Optional<DeliveryAppointment> findBookedByOrderId(String orderId);

    /**
     * 查询订单最近一次预约记录
     */
    Optional<DeliveryAppointment> findLatestByOrderId(String orderId);

    /**
     * 批量查询多个订单当前有效（booked）预约（避免 N+1）
     */
    List<DeliveryAppointment> findBookedByOrderIds(List<String> orderIds);

    DeliveryRecord saveRecord(DeliveryRecord record);

    Optional<DeliveryRecord> findRecordByOrderId(String orderId);
}
