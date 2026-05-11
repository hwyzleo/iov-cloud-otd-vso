package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.DeleteOrderRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteOrderCmd;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 删除订单请求 VO 转换器
 *
 * @author VSO Team
 */
@Mapper
public interface DeleteOrderRequestVoAssembler {

    DeleteOrderRequestVoAssembler INSTANCE = Mappers.getMapper(DeleteOrderRequestVoAssembler.class);

    /**
     * VO 转 Cmd
     */
    DeleteOrderCmd toCmd(DeleteOrderRequestVo vo);
}
