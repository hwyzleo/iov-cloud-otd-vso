package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.SaleModelQuery;
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
    public Optional<SaleModelPo> findBySaleModelCode(String saleModelCode) {
        return Optional.ofNullable(mapper.selectPoByMap(Map.of("saleModelCode", saleModelCode)).stream().findFirst().orElse(null));
    }

    @Override
    public List<SaleModelPo> findAll() {
        return mapper.selectPoByMap(Map.of());
    }

    @Override
    public List<SaleModelPo> findByCondition(SaleModelQuery query) {
        Map<String, Object> params = new HashMap<>();
        if (query.getSaleModelCode() != null && !query.getSaleModelCode().isEmpty()) {
            params.put("saleModelCode", query.getSaleModelCode());
        }
        if (query.getModelName() != null && !query.getModelName().isEmpty()) {
            params.put("modelName", "%" + query.getModelName() + "%");
        }
        if (query.getBeginTime() != null) {
            params.put("beginTime", query.getBeginTime());
        }
        if (query.getEndTime() != null) {
            params.put("endTime", query.getEndTime());
        }
        return mapper.selectPoByMap(params);
    }

    @Override
    public boolean existsBySaleModelCodeExcludeId(String saleModelCode, Long excludeId) {
        List<SaleModelPo> existingList = mapper.selectPoByMap(Map.of("saleModelCode", saleModelCode));
        SaleModelPo existing = existingList.isEmpty() ? null : existingList.get(0);
        if (existing == null) {
            return false;
        }
        return excludeId != null && !existing.getId().equals(excludeId);
    }

    @Override
    public boolean existsByCarlineCode(String carlineCode) {
        if (carlineCode == null || carlineCode.isEmpty()) {
            return false;
        }
        List<SaleModelPo> list = mapper.selectPoByMap(Map.of("carlineCode", carlineCode));
        return !list.isEmpty();
    }

    @Override
    public boolean existsByCarlineCodeExcludeId(String carlineCode, Long excludeId) {
        if (carlineCode == null || carlineCode.isEmpty()) {
            return false;
        }
        List<SaleModelPo> list = mapper.selectPoByMap(Map.of("carlineCode", carlineCode));
        if (list.isEmpty()) {
            return false;
        }
        if (excludeId == null) {
            return true;
        }
        return list.stream().anyMatch(po -> !po.getId().equals(excludeId));
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
