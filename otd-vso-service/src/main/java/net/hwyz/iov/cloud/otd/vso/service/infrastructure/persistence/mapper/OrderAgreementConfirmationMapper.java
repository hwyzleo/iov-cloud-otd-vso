package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAgreementConfirmationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单协议确认快照 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface OrderAgreementConfirmationMapper {

    int insertPo(OrderAgreementConfirmationPo po);

    List<OrderAgreementConfirmationPo> selectByOrderId(@Param("orderId") String orderId);

    OrderAgreementConfirmationPo selectByOrderIdAndAgreementId(@Param("orderId") String orderId,
                                                               @Param("agreementId") String agreementId);
}
