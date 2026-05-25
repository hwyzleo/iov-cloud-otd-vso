package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.enums.AssignStatus;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.VehicleAssignmentVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.VehicleAssignmentResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * 配车信息转换器
 */
@Mapper
public interface VehicleAssignmentAssembler {

    VehicleAssignmentAssembler INSTANCE = Mappers.getMapper(VehicleAssignmentAssembler.class);

    @Mapping(target = "assignStatusName", source = "assignStatus", qualifiedByName = "getAssignStatusName")
    VehicleAssignmentVo toVo(VehicleAssignmentResult result);

    @Named("getAssignStatusName")
    default String getAssignStatusName(String assignStatus) {
        AssignStatus status = AssignStatus.fromCode(assignStatus);
        return status != null ? status.getName() : "";
    }
}