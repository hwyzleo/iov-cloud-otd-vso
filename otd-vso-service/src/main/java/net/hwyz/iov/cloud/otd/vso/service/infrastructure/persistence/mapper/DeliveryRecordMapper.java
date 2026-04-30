package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryRecordPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 交付记录 Mapper 接口
 */
@Mapper
public interface DeliveryRecordMapper extends BaseMapper<DeliveryRecordPo> {

    DeliveryRecordPo selectByOrderId(@Param("orderId") String orderId);

}
