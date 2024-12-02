package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 管理后台销售车型
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SaleModelMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 销售车型名称
     */
    private String modelName;

    /**
     * 销售车型图片集
     */
    private List<String> images;

    /**
     * 是否允许意向金
     */
    private Boolean earnestMoney;

    /**
     * 意向金价格
     */
    private BigDecimal earnestMoneyPrice;

    /**
     * 是否允许定金
     */
    private Boolean downPayment;

    /**
     * 定金价格
     */
    private BigDecimal downPaymentPrice;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

}
