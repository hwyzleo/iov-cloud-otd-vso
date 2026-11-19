package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderResponseVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 订单详情 VO 尾款金额映射测试（OTD-VSO-DSN-CR-014 US-033）
 * <p>
 * 尾款金额只映射服务端订单金额快照事实，缺失时不合成金额。
 *
 * @author VSO Team
 */
@DisplayName("订单详情 VO - 尾款金额映射")
class OrderResponseVoAssemblerTailTest {

    @Test
    @DisplayName("OrderAmount.tailPaymentAmount → OrderResponseVo.tailPaymentAmount")
    void 映射尾款金额() {
        OrderAmount amount = new OrderAmount("AMT1");
        amount.setTailPaymentAmount(new Money(new BigDecimal("183000.00")));
        OrderDetailResult result = OrderDetailResult.builder()
                .orderNo("SO001")
                .orderAmount(amount)
                .build();

        OrderResponseVo vo = OrderResponseVoAssembler.INSTANCE.toVo(result);

        assertEquals(0, new BigDecimal("183000.00").compareTo(vo.getTailPaymentAmount()));
    }

    @Test
    @DisplayName("金额快照缺失：尾款金额为空，不返回 0 或合成值")
    void 金额快照缺失为空() {
        OrderDetailResult result = OrderDetailResult.builder().orderNo("SO001").build();

        OrderResponseVo vo = OrderResponseVoAssembler.INSTANCE.toVo(result);

        assertNull(vo.getTailPaymentAmount());
    }
}
