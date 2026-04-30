package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.WishlistResponseVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * WishlistResponseVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface WishlistResponseVoAssembler {

    WishlistResponseVoAssembler INSTANCE = Mappers.getMapper(WishlistResponseVoAssembler.class);

    WishlistResponseVo toVo(WishlistDetailResult result);

}
