package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建小订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class CreateSmallOrderCmd {

    /**
     * 订单来源
     */
    private String orderSource;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号哈希
     */
    private String mobileHash;

    /**
     * 身份证号哈希
     */
    private String idNoHash;

    /**
     * 车型编码
     */
    private String modelCode;

    /**
     * 车型名称
     */
    private String modelName;

    /**
     * 配置编码
     */
    private String configCode;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 颜色编码
     */
    private String colorCode;

    /**
     * 颜色名称
     */
    private String colorName;

    /**
     * 销售车型编码
     */
    private String saleModelCode;

    /**
     * Variant 编码
     */
    private String variantCode;

    /**
     * OptionCode 列表
     */
    private List<String> optionCodes;

    /**
     * 区域编码
     */
    private String regionCode;
}
