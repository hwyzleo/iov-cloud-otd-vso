package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.FinanceApplicationRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.FinanceApplicationMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.FinanceApplicationPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FinanceApplicationRepositoryImpl implements FinanceApplicationRepository {

    private final FinanceApplicationMapper financeApplicationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceApplicationPo save(FinanceApplicationPo financeApplicationPo) {
        if (financeApplicationPo.getId() == null) {
            financeApplicationMapper.insertPo(financeApplicationPo);
        } else {
            financeApplicationMapper.updatePo(financeApplicationPo);
        }
        return financeApplicationPo;
    }

    @Override
    public Optional<FinanceApplicationPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "applyTime DESC");
        return financeApplicationMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String financeApplicationId) {
        FinanceApplicationPo financeApplicationPo = financeApplicationMapper.selectPoById(Long.valueOf(financeApplicationId));
        if (financeApplicationPo != null) {
            financeApplicationPo.setRowValid(0);
            financeApplicationMapper.updatePo(financeApplicationPo);
        }
    }

}