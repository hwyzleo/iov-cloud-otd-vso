package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.MdmProjectionModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MDM Model 投影仓储实现
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Repository
@RequiredArgsConstructor
public class MdmProjectionModelRepositoryImpl implements MdmProjectionModelRepository {

    private final MdmProjectionModelMapper mapper;

    @Override
    public Optional<MdmProjectionModelPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<MdmProjectionModelPo> findByModelCode(String modelCode) {
        LambdaQueryWrapper<MdmProjectionModelPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MdmProjectionModelPo::getModelCode, modelCode);
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public List<MdmProjectionModelPo> findByCarlineCode(String carlineCode) {
        LambdaQueryWrapper<MdmProjectionModelPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MdmProjectionModelPo::getCarlineCode, carlineCode);
        return mapper.selectList(wrapper);
    }

    @Override
    public List<MdmProjectionModelPo> findAll() {
        return mapper.selectList(null);
    }

    @Override
    public int insert(MdmProjectionModelPo entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(MdmProjectionModelPo entity) {
        return mapper.updateById(entity);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }
}
