package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelBaseModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelBaseModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBaseModelPo;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaleModelBaseModelRepositoryImpl implements SaleModelBaseModelRepository {

    private final SaleModelBaseModelMapper mapper;

    @Override
    public Optional<SaleModelBaseModelPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectPoById(id));
    }

    @Override
    public List<SaleModelBaseModelPo> findBySaleCode(String saleCode) {
        return mapper.selectPoBySaleCode(saleCode);
    }

    @Override
    public Optional<SaleModelBaseModelPo> findBySaleCodeAndBaseModelCode(String saleCode, String baseModelCode) {
        return Optional.ofNullable(mapper.selectPoBySaleCodeAndBaseModelCode(saleCode, baseModelCode));
    }

    @Override
    public Optional<SaleModelBaseModelPo> findBySaleCodeAndBaseModelCodeIncludeDeleted(String saleCode, String baseModelCode) {
        return Optional.empty();
    }

    @Override
    public int insert(SaleModelBaseModelPo entity) {
        return mapper.insertPo(entity);
    }

    @Override
    public int update(SaleModelBaseModelPo entity) {
        return mapper.updatePo(entity);
    }

    @Override
    public int physicalDeleteByIds(Long[] ids) {
        return mapper.physicalDeleteByIds(Arrays.asList(ids));
    }

    @Override
    public int physicalDeleteBySaleCodeAndBaseModelCode(String saleCode, String baseModelCode) {
        return mapper.physicalDeleteBySaleCodeAndBaseModelCode(saleCode, baseModelCode);
    }
}