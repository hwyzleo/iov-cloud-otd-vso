package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedSaleModelRequestVo {

    private String saleCode;
    private String orderNum;
    private Map<String, String> saleModelConfigType;

}
