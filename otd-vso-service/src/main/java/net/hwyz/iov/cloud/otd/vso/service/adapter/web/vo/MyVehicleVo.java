package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 我的车辆 VO（合并心愿单和订单）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyVehicleVo {

    private String id;
    private String type;
    private String displayName;
    private Integer state;
    private Date createTime;
    private Date modifyTime;

    private String saleCode;
    private String buildConfigCode;
    private Map<String, String> saleModelConfigType;
    private Map<String, String> saleModelConfigName;
    private List<String> saleModelImages;
    private BigDecimal totalPrice;
    private Boolean isValid;

}