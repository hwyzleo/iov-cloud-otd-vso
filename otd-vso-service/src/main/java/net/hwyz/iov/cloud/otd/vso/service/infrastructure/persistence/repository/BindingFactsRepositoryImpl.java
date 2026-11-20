package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.BindingFactsRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.BindingFactsMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsDeliveryPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsOrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsPartyPo;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 绑车事实只读仓储实现（OTD-VSO-DSN-CR-017）
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class BindingFactsRepositoryImpl implements BindingFactsRepository {

    private final BindingFactsMapper bindingFactsMapper;

    @Override
    public List<BindingFactsOrderPo> findCandidateOrders(String accountId, String vin, List<String> roles, int limit) {
        if (accountId == null || vin == null || roles == null || roles.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }
        return bindingFactsMapper.selectCandidateOrders(accountId, vin, roles, limit);
    }

    @Override
    public List<BindingFactsPartyPo> findMatchedParties(String accountId, List<String> orderIds, List<String> roles) {
        if (accountId == null || orderIds == null || orderIds.isEmpty() || roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return bindingFactsMapper.selectMatchedParties(accountId, orderIds, roles);
    }

    @Override
    public List<BindingFactsDeliveryPo> findDeliveryRecords(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return bindingFactsMapper.selectDeliveryRecords(orderIds);
    }
}
