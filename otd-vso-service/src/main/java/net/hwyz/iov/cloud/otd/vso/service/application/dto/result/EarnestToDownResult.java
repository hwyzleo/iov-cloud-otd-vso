package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;

/**
 * 意向金转定金结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarnestToDownResult {

    private String orderNo;
    private OrderType orderType;
    private OrderState orderState;
    private SupplementaryPaymentInfo supplementaryPayment;
}
