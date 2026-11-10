package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 支付记录 Mapper 接口
 */
@Mapper
public interface PaymentMapper extends BaseDao<PaymentPo, Long> {

    PaymentPo selectByPaymentNo(@Param("paymentNo") String paymentNo);
    
    /**
     * 根据订单业务 ID 查询支付记录
     *
     * @param orderId 订单业务 ID
     * @return 支付记录列表
     */
    List<PaymentPo> selectByOrderId(@Param("orderId") String orderId);

    PaymentPo selectPoById(Long id);

    /**
     * 同一订单同一阶段的活动支付单
     */
    PaymentPo selectActiveByOrderIdAndStage(@Param("orderId") String orderId,
                                            @Param("paymentStage") String paymentStage);

    /**
     * 订单下全部成功支付单
     */
    List<PaymentPo> selectSuccessfulByOrderId(@Param("orderId") String orderId);

    /**
     * 批量查询多个订单的成功支付单（按支付时间正序）
     */
    List<PaymentPo> selectSuccessfulByOrderIds(@Param("orderIds") List<String> orderIds);

    /**
     * 条件更新支付状态
     */
    int updateStatusConditional(@Param("paymentNo") String paymentNo,
                                @Param("targetStatus") String targetStatus,
                                @Param("expectedStatuses") List<String> expectedStatuses,
                                @Param("externalTradeNo") String externalTradeNo,
                                @Param("payTime") java.time.LocalDateTime payTime,
                                @Param("failReason") String failReason);

    List<PaymentPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(PaymentPo entity);

    int batchInsertPo(List<PaymentPo> entities);

    int updatePo(PaymentPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<PaymentPo> selectPoByExample(PaymentPo example);

}
