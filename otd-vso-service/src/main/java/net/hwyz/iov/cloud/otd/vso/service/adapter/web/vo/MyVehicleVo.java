package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 我的车辆 VO（合并心愿单和订单）
 * 用于首页列表展示，包含缩略信息
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
    private List<String> saleModelImages;
    private BigDecimal totalPrice;
    private String saleModelDesc;

}