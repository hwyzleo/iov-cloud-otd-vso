package net.hwyz.iov.cloud.otd.vso.service.domain.model.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 组织归属值对象
 *
 * @author VSO Team
 */
@Getter
@NoArgsConstructor
public class OrganizationInfo {

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
     * 销售顾问姓名
     */
    private String salesName;

    public OrganizationInfo(String regionCode, String regionName, String storeCode, 
                           String storeName, String salesCode, String salesName) {
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.storeCode = storeCode;
        this.storeName = storeName;
        this.salesCode = salesCode;
        this.salesName = salesName;
    }

    /**
     * 验证归属信息完整性
     */
    public boolean isComplete() {
        return regionCode != null && storeCode != null && salesCode != null;
    }

}
