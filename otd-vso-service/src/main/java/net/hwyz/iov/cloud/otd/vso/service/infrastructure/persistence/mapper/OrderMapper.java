package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderMapper extends BaseDao<OrderPo, Long> {

    /**
     * 根据订单业务 ID 查询订单
     *
     * @param orderId 订单业务 ID
     * @return 订单 PO
     */
    OrderPo selectByOrderId(@Param("orderId") String orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单 PO
     */
    OrderPo selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据小订单号查询订单
     *
     * @param smallOrderNo 小订单号
     * @return 订单 PO
     */
    OrderPo selectBySmallOrderNo(@Param("smallOrderNo") String smallOrderNo);

    /**
     * 逻辑删除订单
     *
     * @param orderNum 订单编号
     * @return 影响行数
     */
    int logicalDeletePoByOrderNum(@Param("orderNum") String orderNum);

    /**
     * 物理删除订单
     *
     * @param orderNum 订单编号
     * @return 影响行数
     */
    int physicalDeletePoByOrderNum(@Param("orderNum") String orderNum);

    /**
     * 根据 Map 对象获取对应数据列表
     *
     * @param params 查询参数
     * @return 订单 PO 列表
     */
    java.util.List<net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo> selectPoByMap(java.util.Map<String, Object> params);

    /**
     * 根据 Map 对象统计记录数
     *
     * @param params 查询参数
     * @return 记录数
     */
    int countPoByMap(java.util.Map<String, Object> params);

    /**
     * 插入订单
     *
     * @param orderPo 订单 PO
     * @return 影响行数
     */
    int insertPo(net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo orderPo);

    /**
     * 更新订单
     *
     * @param orderPo 订单 PO
     * @return 影响行数
     */
    int updatePo(net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo orderPo);
    /**
     * 根据订单号和账号ID查询订单
     *
     * @param orderNo   订单号
     * @param accountId 账号ID
     * @return 订单 PO
     */
    OrderPo selectByOrderNoAndAccountId(@Param("orderNo") String orderNo, @Param("accountId") String accountId);

    /**
     * 根据账号ID查询订单
     *
     * @param accountId 账号ID
     * @param stateList 状态列表
     * @return 订单 PO 列表
     */
    java.util.List<net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo> selectByAccountId(@Param("accountId") String accountId, @Param("stateList") java.util.List<Integer> stateList);
}
