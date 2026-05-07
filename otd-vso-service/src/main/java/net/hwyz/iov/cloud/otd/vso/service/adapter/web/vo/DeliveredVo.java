package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

/**
 * 已交付 Vo
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveredVo {

    /**
     * 订单号
     */
    private String orderNo;

}
