package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 心愿单详情结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class WishlistDetailResult {

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 订单号
     */
    private String orderNum;

    /**
     * 车型配置类型
     */
    private String saleModelConfigType;

    /**
     * 车型配置名称
     */
    private String saleModelConfigName;

    /**
     * 车型配置价格
     */
    private BigDecimal saleModelConfigPrice;

    /**
     * 总价
     */
    private BigDecimal totalPrice;

    /**
     * 车型图片列表
     */
    private List<String> saleModelImages;

    /**
     * 车型描述
     */
    private String saleModelDesc;

    /**
     * 是否有效（配置未变更）
     */
    private Boolean isValid;
}
