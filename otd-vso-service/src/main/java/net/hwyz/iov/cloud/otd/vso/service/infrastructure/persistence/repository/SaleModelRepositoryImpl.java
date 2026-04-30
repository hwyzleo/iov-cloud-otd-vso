package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class SaleModelRepositoryImpl implements SaleModelRepository {

    private final SaleModelMapper mapper;

    @Override
    public Optional<SaleModelPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectPoById(id));
    }

    @Override
    public Optional<SaleModelPo> findBySaleCode(String saleCode) {
        return Optional.ofNullable(mapper.selectPoByMap(Map.of("saleCode", saleCode)).stream().findFirst().orElse(null));
    }

    @Override
    public List<SaleModelPo> findAll() {
        return mapper.selectPoByMap(Map.of());
    }

    @Override
    public List<SaleModelPo> findByCondition(String saleCode, String modelName, Instant beginTime, Instant endTime) {
        Map<String, Object> params = new HashMap<>();
        if (saleCode != null && !saleCode.isEmpty()) {
            params.put("saleCode", saleCode);
        }
        if (modelName != null && !modelName.isEmpty()) {
            params.put("modelName", "%" + modelName + "%");
        }
        if (beginTime != null) {
            params.put("beginTime", beginTime);
        }
        if (endTime != null) {
            params.put("endTime", endTime);
        }
        return mapper.selectPoByMap(params);
    }

    @Override
    public boolean existsBySaleCodeExcludeId(String saleCode, Long excludeId) {
        List<SaleModelPo> existingList = mapper.selectPoByMap(Map.of("saleCode", saleCode));
        SaleModelPo existing = existingList.isEmpty() ? null : existingList.get(0);
        if (existing == null) {
            return false;
        }
        return excludeId != null && !existing.getId().equals(excludeId);
    }

    @Override
    public int insert(SaleModelPo entity) {
        return mapper.insertPo(entity);
    }

    @Override
    public int update(SaleModelPo entity) {
        return mapper.updatePo(entity);
    }

    @Override
    public int physicalDeleteByIds(Long[] ids) {
        return mapper.batchPhysicalDeletePo(ids);
    }
}
