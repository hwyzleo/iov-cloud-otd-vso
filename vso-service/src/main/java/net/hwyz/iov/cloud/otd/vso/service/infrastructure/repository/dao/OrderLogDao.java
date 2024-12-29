package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderLogPo;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 车辆销售订单日志 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-12-29
 */
@Mapper
public interface OrderLogDao extends BaseDao<OrderLogPo, Long> {

}
