package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.MdmProjectionCarlineRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionCarlineMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MDM Carline 投影仓储实现
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Repository
@RequiredArgsConstructor
public class MdmProjectionCarlineRepositoryImpl implements MdmProjectionCarlineRepository {

    private final MdmProjectionCarlineMapper mapper;

    @Override
    public Optional<MdmProjectionCarlinePo> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<MdmProjectionCarlinePo> findByCarlineCode(String carlineCode) {
        LambdaQueryWrapper<MdmProjectionCarlinePo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MdmProjectionCarlinePo::getCarlineCode, carlineCode);
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public List<MdmProjectionCarlinePo> findAll() {
        return mapper.selectList(null);
    }

    @Override
    public int insert(MdmProjectionCarlinePo entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(MdmProjectionCarlinePo entity) {
        return mapper.updateById(entity);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }
}
