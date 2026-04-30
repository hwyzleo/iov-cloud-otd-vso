package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

/**
 * 待交付 Vo
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepareDeliveryVo {

    /**
     * 订单号
     */
    private String orderNum;

}
