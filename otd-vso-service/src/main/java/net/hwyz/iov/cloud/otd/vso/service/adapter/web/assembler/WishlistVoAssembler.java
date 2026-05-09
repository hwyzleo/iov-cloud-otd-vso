package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigItemResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 心愿单 Vo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface WishlistVoAssembler {

    WishlistVoAssembler INSTANCE = Mappers.getMapper(WishlistVoAssembler.class);

    @Mapping(target = "accountId", source = "accountId")
    CreateWishlistCmd toCreateWishlistCmd(String accountId, CreateWishlistRequestVo vo);

    @Mapping(target = "accountId", source = "accountId")
    ModifyWishlistCmd toModifyWishlistCmd(String accountId, ModifyWishlistRequestVo vo);

    @Mapping(target = "accountId", source = "accountId")
    DeleteWishlistCmd toDeleteWishlistCmd(String accountId, DeleteWishlistRequestVo vo);

    WishlistListVo toVo(WishlistListResult result);

    List<WishlistListVo> toVoList(List<WishlistListResult> results);

    WishlistDetailVo toDetailVo(WishlistDetailResult result);

    SaleModelConfigItemVo toConfigItemVo(SaleModelConfigItemResult result);

    List<SaleModelConfigItemVo> toConfigItemVoList(List<SaleModelConfigItemResult> results);

}