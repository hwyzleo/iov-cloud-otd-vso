package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;

/**
 * 发起支付命令
 *
 * @author hwyz_leo
 */
@Data
@Builder
public class InitiatePaymentCmd {

    private String accountId;
    private String orderNo;
    private PaymentChannel paymentChannel;

}