package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderModelConfig;

import java.util.Map;

/**
 * 定金订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class DownPaymentCmd {

    private String accountId;
    private String orderNo;
    private String saleModel;
    private String buildConfigCode;
    private Map<String, OrderModelConfig> modelConfigMap;
    private String customerType;
    private String paymentMethod;
    private Integer orderPersonType;
    private String orderPersonName;
    private Integer orderPersonIdType;
    private String orderPersonIdNum;
    private Integer purchasePlan;
    private String licenseCityCode;
    private String dealership;
    private String deliveryCenter;
    private String wishlistId;

}
