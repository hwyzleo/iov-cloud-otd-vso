package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 车辆销售订单 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-19
 */
@Mapper
public interface OrderDao extends BaseDao<OrderPo, Long> {

    /**
     * 物理删除订单
     *
     * @param orderNum 订单编号
     * @return 影响行数
     */
    int physicalDeletePoByOrderNum(String orderNum);

}
