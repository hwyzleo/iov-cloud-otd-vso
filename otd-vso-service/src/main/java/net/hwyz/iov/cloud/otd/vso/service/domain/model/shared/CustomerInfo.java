package net.hwyz.iov.cloud.otd.vso.service.domain.model.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 客户信息值对象
 *
 * @author VSO Team
 */
@Getter
@NoArgsConstructor
public class CustomerInfo {

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号哈希（用于脱敏和查询）
     */
    private String mobileHash;

    /**
     * 身份证号哈希（用于脱敏和查询）
     */
    private String idNoHash;

    /**
     * 客户类型
     */
    private String customerType;

    public CustomerInfo(String userId, String name, String mobileHash, String idNoHash, String customerType) {
        this.userId = userId;
        this.name = name;
        this.mobileHash = mobileHash;
        this.idNoHash = idNoHash;
        this.customerType = customerType != null ? customerType : "personal";
    }

    /**
     * 验证客户信息完整性
     */
    public boolean isComplete() {
        return userId != null && name != null && !name.trim().isEmpty();
    }

}
