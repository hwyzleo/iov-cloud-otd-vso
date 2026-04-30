package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPaymentPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单支付记录 Mapper
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-19
 */
@Mapper
public interface OrderPaymentMapper extends BaseDao<OrderPaymentPo, Long> {

}
