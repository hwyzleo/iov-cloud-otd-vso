package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderLogPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 车辆销售订单日志 Mapper
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-12-29
 */
@Mapper
public interface OrderLogMapper extends BaseDao<OrderLogPo, Long> {

}
