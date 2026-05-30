package net.hwyz.iov.cloud.otd.vso.service.application.assembler;

import cn.hutool.json.JSONUtil;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * 销售车型结果 Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface SaleModelResultAssembler {

    SaleModelResultAssembler INSTANCE = Mappers.getMapper(SaleModelResultAssembler.class);

    @Mapping(target = "images", expression = "java(stringToList(po.getImages()))")
    @Mapping(target = "availableRegions", expression = "java(stringToList(po.getAvailableRegions()))")
    @Mapping(target = "channels", expression = "java(stringToList(po.getChannels()))")
    SaleModelResult toResult(SaleModelPo po);

    List<SaleModelResult> toResultList(List<SaleModelPo> poList);

    @Mapping(target = "typeImage", source = "typeImage")
    SaleModelConfigResult toConfigResult(SaleModelConfigPo po);

    List<SaleModelConfigResult> toConfigResultList(List<SaleModelConfigPo> poList);

    default List<String> stringToList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        return JSONUtil.toList(json, String.class);
    }

    default Instant timestampToInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
