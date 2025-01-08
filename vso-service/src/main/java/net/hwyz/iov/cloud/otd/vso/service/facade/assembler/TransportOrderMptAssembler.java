package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.TransportOrderMpt;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台运输相关车辆销售订单转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface TransportOrderMptAssembler {

    TransportOrderMptAssembler INSTANCE = Mappers.getMapper(TransportOrderMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param orderPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    TransportOrderMpt fromPo(OrderPo orderPo);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param orderPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<TransportOrderMpt> fromPoList(List<OrderPo> orderPoList);

}
