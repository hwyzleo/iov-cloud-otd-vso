package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.TransportOrderMpt;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 发运申请信息回显映射测试（OTD-VSO-DSN-CR-014）
 * <p>
 * 事故现象：管理后台点击“申请发运”后，时间线已有节点、数据库 vso_order 也已写入
 * transport_apply_person_id/name/time，但运输列表的“发运申请人/发运申请时间”仍为空。
 * 原因是 {@link OrderListResult} 缺少这三个字段，MapStruct 在
 * Order → OrderListResult → TransportOrderMpt 两段映射中把它们丢掉了。
 *
 * @author VSO Team
 */
@DisplayName("发运申请信息回显映射")
class TransportApplyMappingTest {

    private static final Date APPLY_TIME = new Date(1763441727412L);

    @Test
    @DisplayName("申请发运后，运输列表 VO 必须回显申请人 ID/姓名与申请时间")
    void shouldCarryTransportApplyInfoFromOrderToListVo() {
        Order order = Order.builder()
                .id("ORD-1")
                .orderNo("VSO2611180001")
                .orderState(OrderState.APPLY_TRANSPORT)
                .transportApplyPersonId("admin")
                .transportApplyPersonName("admin")
                .transportApplyTime(APPLY_TIME)
                .build();

        OrderListResult result = OrderDtoAssembler.INSTANCE.toOrderListResult(order);
        assertThat(result.getTransportApplyPersonId()).isEqualTo("admin");
        assertThat(result.getTransportApplyPersonName()).isEqualTo("admin");
        assertThat(result.getTransportApplyTime()).isEqualTo(APPLY_TIME);

        TransportOrderMpt vo = TransportOrderMptAssembler.INSTANCE.toVo(result);
        assertThat(vo.getTransportApplyPersonId()).isEqualTo("admin");
        assertThat(vo.getTransportApplyPersonName()).isEqualTo("admin");
        assertThat(vo.getTransportApplyTime()).isEqualTo(APPLY_TIME);
    }

    @Test
    @DisplayName("未申请发运的订单（450 等）上述字段保持为空，不影响按钮可用性判断")
    void shouldKeepTransportApplyInfoEmptyBeforeApply() {
        Order order = Order.builder()
                .id("ORD-2")
                .orderNo("VSO2611180002")
                .orderState(OrderState.ALLOCATION_VEHICLE)
                .build();

        TransportOrderMpt vo = TransportOrderMptAssembler.INSTANCE
                .toVo(OrderDtoAssembler.INSTANCE.toOrderListResult(order));

        assertThat(vo.getTransportApplyPersonId()).isNull();
        assertThat(vo.getTransportApplyPersonName()).isNull();
        assertThat(vo.getTransportApplyTime()).isNull();
        assertThat(vo.getOrderState()).isEqualTo(OrderState.ALLOCATION_VEHICLE.getValue());
    }
}
