package net.hwyz.iov.cloud.otd.vso.service.domain.order.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderDo;
import net.hwyz.iov.cloud.tsp.framework.commons.domain.BaseRepository;

/**
 * 车辆销售订单领域仓库接口
 *
 * @author hwyz_leo
 */
public interface OrderRepository extends BaseRepository<String, OrderDo> {

    /**
     * 获取订单
     *
     * @param orderPersonId 下单人员ID
     * @param orderNum      订单编号
     * @return 订单领域对象
     */
    OrderDo get(String orderPersonId, String orderNum);

}
