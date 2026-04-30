package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryAppointmentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 交付预约 Mapper 接口
 */
@Mapper
public interface DeliveryAppointmentMapper extends BaseMapper<DeliveryAppointmentPo> {

    DeliveryAppointmentPo selectByAppointmentNo(@Param("appointmentNo") String appointmentNo);

}
