package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAgreementConfirmation;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAgreementConfirmationRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.PurchaseAgreementPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderAgreementConfirmationMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单协议确认快照仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class OrderAgreementConfirmationRepositoryImpl implements OrderAgreementConfirmationRepository {

    private final OrderAgreementConfirmationMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAgreementConfirmation save(OrderAgreementConfirmation confirmation) {
        mapper.insertPo(PurchaseAgreementPoConverter.fromDomain(confirmation));
        return confirmation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderAgreementConfirmation> saveAll(List<OrderAgreementConfirmation> confirmations) {
        confirmations.forEach(this::save);
        return confirmations;
    }

    @Override
    public List<OrderAgreementConfirmation> findByOrderId(String orderId) {
        return mapper.selectByOrderId(orderId).stream()
                .map(PurchaseAgreementPoConverter::toDomain)
                .collect(Collectors.toList());
    }
}
