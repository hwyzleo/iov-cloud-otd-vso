package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryAppointmentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 交付预约 Mapper 接口
 */
@Mapper
public interface DeliveryAppointmentMapper extends BaseDao<DeliveryAppointmentPo, Long> {

    DeliveryAppointmentPo selectByAppointmentNo(@Param("appointmentNo") String appointmentNo);

    DeliveryAppointmentPo selectPoById(Long id);

    List<DeliveryAppointmentPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(DeliveryAppointmentPo entity);

    int batchInsertPo(List<DeliveryAppointmentPo> entities);

    int updatePo(DeliveryAppointmentPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<DeliveryAppointmentPo> selectPoByExample(DeliveryAppointmentPo example);

    List<DeliveryAppointmentPo> selectByOrderId(@Param("orderId") String orderId);

}
