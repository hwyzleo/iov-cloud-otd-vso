package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelConfigMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaleModelConfigRepositoryImpl implements SaleModelConfigRepository {

    private final SaleModelConfigMapper mapper;
    private final SaleModelRepository saleModelRepository;

    @Override
    public Optional<SaleModelConfigPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectPoById(id));
    }

    @Override
    public List<SaleModelConfigPo> findBySaleModelCode(String saleModelCode) {
        return mapper.selectPoBySaleModelCode(saleModelCode);
    }

    @Override
    public List<SaleModelConfigPo> findBySaleModelId(Long saleModelId) {
        Optional<SaleModelPo> saleModelOpt = saleModelRepository.findById(saleModelId);
        if (saleModelOpt.isEmpty()) {
            return List.of();
        }
        return findBySaleModelCode(saleModelOpt.get().getSaleModelCode());
    }

    @Override
    public Optional<SaleModelConfigPo> findByIdAndSaleModelCode(Long id, String saleModelCode) {
        return Optional.ofNullable(mapper.selectPoByIdAndSaleModelCode(id, saleModelCode));
    }

    @Override
    public int insert(SaleModelConfigPo entity) {
        return mapper.insertPo(entity);
    }

    @Override
    public int update(SaleModelConfigPo entity) {
        return mapper.updatePo(entity);
    }

    @Override
    public int physicalDeleteBySaleModelCodeAndIds(String saleModelCode, Long[] ids) {
        return mapper.batchPhysicalDeletePo(saleModelCode, ids);
    }
}
