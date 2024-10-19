package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPaymentPo;
import net.hwyz.iov.cloud.tsp.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单支付记录 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-19
 */
@Mapper
public interface OrderPaymentDao extends BaseDao<OrderPaymentPo, Long> {

}
