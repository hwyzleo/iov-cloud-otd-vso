package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAgreementConfirmation;

import java.util.List;

/**
 * 订单协议确认快照仓储接口
 *
 * @author VSO Team
 */
public interface OrderAgreementConfirmationRepository {

    OrderAgreementConfirmation save(OrderAgreementConfirmation confirmation);

    List<OrderAgreementConfirmation> saveAll(List<OrderAgreementConfirmation> confirmations);

    List<OrderAgreementConfirmation> findByOrderId(String orderId);
}
