package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SubsidyApplicationRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SubsidyApplicationMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SubsidyApplicationPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubsidyApplicationRepositoryImpl implements SubsidyApplicationRepository {

    private final SubsidyApplicationMapper subsidyApplicationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubsidyApplicationPo save(SubsidyApplicationPo subsidyApplicationPo) {
        if (subsidyApplicationPo.getId() == null) {
            subsidyApplicationMapper.insertPo(subsidyApplicationPo);
        } else {
            subsidyApplicationMapper.updatePo(subsidyApplicationPo);
        }
        return subsidyApplicationPo;
    }

    @Override
    public Optional<SubsidyApplicationPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "applyTime DESC");
        return subsidyApplicationMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String subsidyApplicationId) {
        SubsidyApplicationPo subsidyApplicationPo = subsidyApplicationMapper.selectPoById(Long.valueOf(subsidyApplicationId));
        if (subsidyApplicationPo != null) {
            subsidyApplicationPo.setRowValid(0);
            subsidyApplicationMapper.updatePo(subsidyApplicationPo);
        }
    }

}