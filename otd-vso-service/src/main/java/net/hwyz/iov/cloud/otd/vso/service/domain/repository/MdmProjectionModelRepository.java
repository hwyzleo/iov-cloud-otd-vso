package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;

import java.util.List;
import java.util.Optional;

/**
 * MDM Model 投影仓储接口
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
public interface MdmProjectionModelRepository {

    Optional<MdmProjectionModelPo> findById(Long id);

    Optional<MdmProjectionModelPo> findByModelCode(String modelCode);

    List<MdmProjectionModelPo> findByCarlineCode(String carlineCode);

    List<MdmProjectionModelPo> findAll();

    int insert(MdmProjectionModelPo entity);

    int update(MdmProjectionModelPo entity);

    int deleteById(Long id);
}
