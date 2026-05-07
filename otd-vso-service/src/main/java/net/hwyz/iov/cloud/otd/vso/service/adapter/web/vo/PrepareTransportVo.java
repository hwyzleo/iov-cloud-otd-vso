package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

/**
 * 待运输 Vo
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepareTransportVo {

    /**
     * 订单号
     */
    private String orderNo;

}
