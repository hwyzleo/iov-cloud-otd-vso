package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelBuildConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelBuildConfigMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBuildConfigPo;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaleModelBuildConfigRepositoryImpl implements SaleModelBuildConfigRepository {

    private final SaleModelBuildConfigMapper mapper;

    @Override
    public Optional<SaleModelBuildConfigPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectPoById(id));
    }

    @Override
    public List<SaleModelBuildConfigPo> findBySaleModelCode(String saleModelCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("saleModelCode", saleModelCode);
        List<SaleModelBuildConfigPo> poList = mapper.selectPoByMap(map);
        return PageUtil.convert(poList, po -> po);
    }

    @Override
    public Optional<SaleModelBuildConfigPo> findBySaleModelCodeAndBuildConfigCode(String saleModelCode, String buildConfigCode) {
        SaleModelBuildConfigPo example = new SaleModelBuildConfigPo();
        example.setSaleModelCode(saleModelCode);
        example.setBuildConfigCode(buildConfigCode);
        example.setRowValid(true);
        List<SaleModelBuildConfigPo> list = mapper.selectPoByExample(example);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<SaleModelBuildConfigPo> findBySaleModelCodeAndBuildConfigCodeIncludeDeleted(String saleModelCode, String buildConfigCode) {
        SaleModelBuildConfigPo example = new SaleModelBuildConfigPo();
        example.setSaleModelCode(saleModelCode);
        example.setBuildConfigCode(buildConfigCode);
        List<SaleModelBuildConfigPo> list = mapper.selectPoByExample(example);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public int insert(SaleModelBuildConfigPo entity) {
        return mapper.insertPo(entity);
    }

    @Override
    public int update(SaleModelBuildConfigPo entity) {
        return mapper.updatePo(entity);
    }

    @Override
    public int physicalDeleteByIds(Long[] ids) {
        return mapper.physicalDeleteByIds(Arrays.asList(ids));
    }

    @Override
    public int physicalDeleteBySaleModelCodeAndBuildConfigCode(String saleModelCode, String buildConfigCode) {
        return mapper.physicalDeleteBySaleModelCodeAndBuildConfigCode(saleModelCode, buildConfigCode);
    }

    @Override
    public int physicalDeleteBySaleModelCode(String saleModelCode) {
        return mapper.physicalDeleteBySaleModelCode(saleModelCode);
    }

    @Override
    public int countBySaleModelCode(String saleModelCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("saleModelCode", saleModelCode);
        return mapper.countPoByMap(map);
    }
}