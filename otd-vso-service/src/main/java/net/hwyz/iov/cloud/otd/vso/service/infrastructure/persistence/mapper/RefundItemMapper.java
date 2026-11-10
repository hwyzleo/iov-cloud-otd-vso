package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundItemPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 退款原支付明细 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface RefundItemMapper {

    int insertPo(RefundItemPo po);

    int updatePo(RefundItemPo po);

    List<RefundItemPo> selectByRefundId(@Param("refundId") String refundId);

    List<RefundItemPo> selectByRefundIdAndStatus(@Param("refundId") String refundId,
                                                 @Param("refundStatus") String refundStatus);

    List<RefundItemPo> selectByPaymentIdAndStatus(@Param("paymentId") String paymentId,
                                                  @Param("refundStatus") String refundStatus);

    RefundItemPo selectByRefundItemId(@Param("refundItemId") String refundItemId);
}
