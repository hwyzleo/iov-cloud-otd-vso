package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 意向金转定金命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class EarnestToDownCmd {

    private String accountId;
    private String orderNo;
    
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

}
