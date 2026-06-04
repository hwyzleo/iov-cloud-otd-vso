package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.DeleteWishlistRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CancelCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestToDownCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.LockCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * OrderVo Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface OrderVoAssembler {

    OrderVoAssembler INSTANCE = Mappers.getMapper(OrderVoAssembler.class);

    OrderVo toVo(OrderListResult result);

    List<OrderVo> toVoList(List<OrderListResult> results);

    @Mapping(target = "accountId", source = "accountId")
    DeleteWishlistCmd toDeleteWishlistCmd(String accountId, DeleteWishlistRequestVo vo);

    @Mapping(target = "accountId", source = "accountId")
    CancelCmd toCancelCmd(String accountId, OrderVo vo);

    @Mapping(target = "accountId", source = "accountId")
    EarnestToDownCmd toEarnestToDownCmd(String accountId, OrderVo vo);

    @Mapping(target = "accountId", source = "accountId")
    LockCmd toLockCmd(String accountId, OrderVo vo);

}
