package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.PhysicalDeleteResponseVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PhysicalDeleteResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 物理删除响应 VO 转换器
 *
 * @author VSO Team
 */
@Mapper
public interface PhysicalDeleteResponseVoAssembler {

    PhysicalDeleteResponseVoAssembler INSTANCE = Mappers.getMapper(PhysicalDeleteResponseVoAssembler.class);

    /**
     * Result 转 Vo
     */
    PhysicalDeleteResponseVo toVo(PhysicalDeleteResult result);
}
