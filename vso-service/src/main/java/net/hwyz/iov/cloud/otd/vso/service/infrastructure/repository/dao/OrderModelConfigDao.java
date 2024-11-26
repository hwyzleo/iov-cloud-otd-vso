package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderModelConfigPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单车型配置 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-11
 */
@Mapper
public interface OrderModelConfigDao extends BaseDao<OrderModelConfigPo, Long> {

    /**
     * 根据订单号批量物理删除订单车型配置
     *
     * @param orderNum 订单号
     * @return 操作记录数
     */
    int batchPhysicalDeletePoByOrderNum(String orderNum);

}
