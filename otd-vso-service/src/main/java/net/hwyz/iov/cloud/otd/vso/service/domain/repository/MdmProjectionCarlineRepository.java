package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;

import java.util.List;
import java.util.Optional;

/**
 * MDM Carline 投影仓储接口
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
public interface MdmProjectionCarlineRepository {

    Optional<MdmProjectionCarlinePo> findById(Long id);

    Optional<MdmProjectionCarlinePo> findByCarlineCode(String carlineCode);

    List<MdmProjectionCarlinePo> findAll();

    int insert(MdmProjectionCarlinePo entity);

    int update(MdmProjectionCarlinePo entity);

    int deleteById(Long id);
}
