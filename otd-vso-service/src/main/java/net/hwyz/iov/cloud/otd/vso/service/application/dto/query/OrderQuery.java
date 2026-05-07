package net.hwyz.iov.cloud.otd.vso.service.application.dto.query;

import lombok.Builder;
import lombok.Data;

/**
 * 订单查询参数
 *
 * @author VSO Team
 */
@Data
@Builder
public class OrderQuery {

    private String orderNo;
    private Integer orderState;
    private java.util.List<Integer> orderStateRange;
    private Boolean hasDeliveryPerson;
    private java.util.Date beginTime;
    private java.util.Date endTime;
    private String type;
    private String accountId;
    private String deliveryPersonId;
    private Boolean delivered;

}
