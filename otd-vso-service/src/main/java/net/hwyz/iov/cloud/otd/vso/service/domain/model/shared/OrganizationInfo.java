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
     * 归属区域编码
     */
    private String ownerRegionCode;

    /**
     * 归属区域名称
     */
    private String ownerRegionName;

    /**
     * 归属门店编码
     */
    private String ownerStoreCode;

    /**
     * 归属门店名称
     */
    private String ownerStoreName;

    /**
     * 销售顾问编码
     */
    private String salesCode;

    /**
     * 销售顾问姓名
     */
    private String salesName;

    public OrganizationInfo(String ownerRegionCode, String ownerRegionName, String ownerStoreCode, 
                           String ownerStoreName, String salesCode, String salesName) {
        this.ownerRegionCode = ownerRegionCode;
        this.ownerRegionName = ownerRegionName;
        this.ownerStoreCode = ownerStoreCode;
        this.ownerStoreName = ownerStoreName;
        this.salesCode = salesCode;
        this.salesName = salesName;
    }

    /**
     * 验证归属信息完整性
     */
    public boolean isComplete() {
        return ownerRegionCode != null && ownerStoreCode != null && salesCode != null;
    }

}