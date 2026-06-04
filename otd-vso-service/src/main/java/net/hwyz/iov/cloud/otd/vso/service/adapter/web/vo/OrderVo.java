package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单列表视图对象
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVo {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态
     */
    private Integer orderState;

    /**
     * 下单时间
     */
    private Date orderTime;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 购车人类型
     */
    private Integer orderPersonType;

    /**
     * 购车人姓名
     */
    private String orderPersonName;

    /**
     * 购车人证件类型
     */
    private Integer orderPersonIdType;

    /**
     * 购车人证件号码
     */
    private String orderPersonIdNum;

    /**
     * 购车计划
     */
    private Integer purchasePlan;

    /**
     * 上牌城市代码
     */
    private String licenseCityCode;

    /**
     * 下单门店编码
     */
    private String orderStoreCode;

    /**
     * 交付门店编码
     */
    private String deliveryStoreCode;

    /**
     * 销售车型代码
     */
    private String saleModelCode;

    /**
     * 车型代码
     */
    private String modelCode;

    /**
     * 车型名称
     */
    private String modelName;

    /**
     * 版本代码
     */
    private String variantCode;

    /**
     * 版本名称
     */
    private String variantName;

    /**
     * 配置代码
     */
    private String configurationCode;

    /**
     * 选装代码列表
     */
    private List<String> optionCodes;

    /**
     * 选装明细
     */
    private List<OptionBreakdownItem> optionBreakdown;

    /**
     * 总价
     */
    private BigDecimal totalPrice;

    /**
     * 销售车型图片列表
     */
    private List<String> saleModelImages;

    /**
     * 销售车型描述
     */
    private String saleModelDesc;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionBreakdownItem {
        private String optionCode;
        private String optionFamilyCode;
        private String optionFamilyName;
        private String optionName;
        private BigDecimal optionPrice;
    }

}
