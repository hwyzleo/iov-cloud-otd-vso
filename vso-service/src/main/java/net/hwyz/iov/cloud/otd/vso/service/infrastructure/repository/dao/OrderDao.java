package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import net.hwyz.iov.cloud.tsp.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 车辆销售订单 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-10
 */
@Mapper
public interface OrderDao extends BaseDao<OrderPo, Long> {

    /**
     * 根据订单编号删除订单
     *
     * @param orderNum 订单编号
     * @return 影响记录数
     */
    int physicalDeletePoByOrderNum(String orderNum);

}
