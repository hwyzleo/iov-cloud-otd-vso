package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

/**
 * 激活车辆 Vo
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateVo {

    /**
     * 订单号
     */
    private String orderNum;

}
