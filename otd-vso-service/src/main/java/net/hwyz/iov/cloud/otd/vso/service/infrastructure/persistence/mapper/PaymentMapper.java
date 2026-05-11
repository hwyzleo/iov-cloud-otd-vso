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

    List<PaymentPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(PaymentPo entity);

    int batchInsertPo(List<PaymentPo> entities);

    int updatePo(PaymentPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<PaymentPo> selectPoByExample(PaymentPo example);

}
