package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVo {

    private String orderNo;
    private Integer orderState;
    private String displayName;

}
