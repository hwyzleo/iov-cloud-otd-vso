package net.hwyz.iov.cloud.otd.vso.service.facade.assembler;

import net.hwyz.iov.cloud.otd.vso.api.contract.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台车辆销售订单转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface VehicleSaleOrderMptAssembler {

    VehicleSaleOrderMptAssembler INSTANCE = Mappers.getMapper(VehicleSaleOrderMptAssembler.class);

    /**
     * 数据对象转数据传输对象
     *
     * @param orderPo 数据对象
     * @return 数据传输对象
     */
    @Mappings({})
    VehicleSaleOrderMpt fromPo(OrderPo orderPo);

    /**
     * 数据对象列表转数据传输对象列表
     *
     * @param orderPoList 数据对象列表
     * @return 数据传输对象列表
     */
    List<VehicleSaleOrderMpt> fromPoList(List<OrderPo> orderPoList);

}
