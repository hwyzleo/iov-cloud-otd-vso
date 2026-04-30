package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 创建正式订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class CreateFormalOrderCmd {

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
     * 区域编码
     */
    private String regionCode;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 门店编码
     */
    private String storeCode;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 销售顾问编码
     */
    private String salesCode;

    /**
     * 销售顾问名称
     */
    private String salesName;
}
