package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import net.hwyz.iov.cloud.otd.vso.api.enums.AssignStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.AssignmentType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.VehicleAssignment;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.VehicleAssignmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 配车仓储 - 领域模型持久化语义测试。
 * <p>
 * 回归背景：{@code VehicleAssignment} 领域模型缺失持久化主键，导致 {@code saveDomain} 在更新已有
 * 配车记录时被判定为新增，触发 {@code uk_vehicle_assignment_id} 唯一键冲突（DuplicateKeyException），
 * 使 VIN 超时释放、换绑、解绑、订单取消释放等更新链路全部失败。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("配车仓储 - 主键回填与新增/更新判定")
class VehicleAssignmentRepositoryImplTest {

    private static final Long PO_ID = 1024L;
    private static final String ASSIGNMENT_ID = "e17e3f614dbc41979d3ee914b9ef6b88";

    @Mock
    private VehicleAssignmentMapper vehicleAssignmentMapper;

    @InjectMocks
    private VehicleAssignmentRepositoryImpl vehicleAssignmentRepository;

    @Test
    @DisplayName("已存在的配车记录走更新，不产生重复插入")
    void 已存在记录保存走更新() {
        VehicleAssignment assignment = existingAssignment();
        assignment.expire();

        when(vehicleAssignmentMapper.updatePo(any(VehicleAssignmentPo.class))).thenReturn(1);

        vehicleAssignmentRepository.saveDomain(assignment);

        ArgumentCaptor<VehicleAssignmentPo> captor = ArgumentCaptor.forClass(VehicleAssignmentPo.class);
        verify(vehicleAssignmentMapper).updatePo(captor.capture());
        verify(vehicleAssignmentMapper, never()).insertPo(any(VehicleAssignmentPo.class));

        VehicleAssignmentPo saved = captor.getValue();
        assertEquals(PO_ID, saved.getId(), "更新时必须携带持久化主键");
        assertEquals(ASSIGNMENT_ID, saved.getVehicleAssignmentId());
        assertEquals(AssignmentType.EXPIRE.getCode(), saved.getAssignmentType(), "动作类型需随领域变更落库");
        assertEquals(AssignStatus.EXPIRED.getCode(), saved.getAssignStatus());
        assertEquals("SELF", saved.getVehicleSourceType(), "车源类型不应在保存时丢失");
    }

    @Test
    @DisplayName("新建配车记录走插入")
    void 新建记录保存走插入() {
        VehicleAssignment assignment = VehicleAssignment.assign("VbVflJ8wigxIX07", "HWYZTEST900000001", 24);

        when(vehicleAssignmentMapper.insertPo(any(VehicleAssignmentPo.class))).thenReturn(1);

        vehicleAssignmentRepository.saveDomain(assignment);

        ArgumentCaptor<VehicleAssignmentPo> captor = ArgumentCaptor.forClass(VehicleAssignmentPo.class);
        verify(vehicleAssignmentMapper).insertPo(captor.capture());
        verify(vehicleAssignmentMapper, never()).updatePo(any(VehicleAssignmentPo.class));

        VehicleAssignmentPo saved = captor.getValue();
        assertEquals(null, saved.getId(), "新增记录不应携带主键");
        assertEquals(AssignStatus.ASSIGNED.getCode(), saved.getAssignStatus());
        assertEquals(assignment.getVehicleAssignmentId(), saved.getVehicleAssignmentId());
    }

    @Test
    @DisplayName("按业务 ID 逻辑删除，不将业务 ID 当自增主键解析")
    void 按业务ID逻辑删除() {
        VehicleAssignmentPo po = new VehicleAssignmentPo();
        po.setId(PO_ID);
        po.setVehicleAssignmentId(ASSIGNMENT_ID);
        when(vehicleAssignmentMapper.selectByVehicleAssignmentId(ASSIGNMENT_ID)).thenReturn(po);
        when(vehicleAssignmentMapper.logicalDeletePo(PO_ID)).thenReturn(1);

        vehicleAssignmentRepository.delete(ASSIGNMENT_ID);

        verify(vehicleAssignmentMapper).logicalDeletePo(PO_ID);
        verify(vehicleAssignmentMapper, never()).selectPoById(any());
    }

    @Test
    @DisplayName("配车记录不存在时删除为幂等空操作")
    void 记录不存在时删除幂等() {
        when(vehicleAssignmentMapper.selectByVehicleAssignmentId(ASSIGNMENT_ID)).thenReturn(null);

        vehicleAssignmentRepository.delete(ASSIGNMENT_ID);

        verify(vehicleAssignmentMapper, never()).logicalDeletePo(any());
    }

    private VehicleAssignment existingAssignment() {
        return VehicleAssignment.builder()
                .id(PO_ID)
                .vehicleAssignmentId(ASSIGNMENT_ID)
                .orderId("VbVflJ8wigxIX07")
                .assignmentType(AssignmentType.ASSIGN)
                .vehicleSourceType("SELF")
                .vin("HWYZTEST900000001")
                .assignStatus(AssignStatus.ASSIGNED)
                .manualAssignFlag(1)
                .occupyExpireTime(java.time.LocalDateTime.now().minusHours(1))
                .assignTime(java.time.LocalDateTime.now().minusHours(25))
                .build();
    }
}
