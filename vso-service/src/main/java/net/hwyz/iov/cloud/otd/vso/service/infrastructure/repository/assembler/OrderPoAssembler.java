package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.assembler;

import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderDo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 车辆销售订单数据对象转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface OrderPoAssembler {

    OrderPoAssembler INSTANCE = Mappers.getMapper(OrderPoAssembler.class);

    /**
     * 数据对象转领域对象
     *
     * @param orderPo 数据对象
     * @return 领域对象
     */
    @Mappings({
            @Mapping(target="orderState", expression = "java(net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState.valOf(orderPo.getOrderState()))"),
            @Mapping(target="payState", expression = "java(net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.PayState.valOf(orderPo.getPayState()))")
    })
    OrderDo toDo(OrderPo orderPo);

    /**
     * 领域对象转数据对象
     *
     * @param orderDo 领域对象
     * @return 数据对象
     */
    @Mappings({
            @Mapping(target="orderState", source = "orderState.value"),
            @Mapping(target="payState", source = "payState.value")
    })
    OrderPo fromDo(OrderDo orderDo);

}
