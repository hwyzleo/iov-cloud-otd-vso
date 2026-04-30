package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderModelConfig;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderModelConfigPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 车辆销售订单车型配置数据对象转换类
 *
 * @author hwyz_leo
 */
@Mapper
public interface OrderModelConfigPoConverter {

    OrderModelConfigPoConverter INSTANCE = Mappers.getMapper(OrderModelConfigPoConverter.class);

    /**
     * 数据对象转领域对象
     *
     * @param orderPo 数据对象
     * @return 领域对象
     */
    @Mappings({})
    OrderModelConfig toDo(OrderModelConfigPo orderPo);

    /**
     * 领域对象转数据对象
     *
     * @param orderModelConfig 领域对象
     * @return 数据对象
     */
    @Mappings({})
    OrderModelConfigPo fromDo(OrderModelConfig orderModelConfig);

    /**
     * 领域对象列表转数据对象列表
     *
     * @param orderModelConfigList 领域对象列表
     * @return 数据对象列表
     */
    List<OrderModelConfigPo> fromDoList(List<OrderModelConfig> orderModelConfigList);

}
