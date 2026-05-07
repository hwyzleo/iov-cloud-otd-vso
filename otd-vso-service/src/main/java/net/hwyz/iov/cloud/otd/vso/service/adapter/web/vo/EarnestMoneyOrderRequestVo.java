package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarnestMoneyOrderRequestVo {

    private String saleCode;
    private String orderNo;
    private Map<String, String> saleModelConfigType;
    private String licenseCityCode;

}
