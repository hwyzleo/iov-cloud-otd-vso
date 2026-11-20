package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import lombok.Data;

import java.io.Serializable;

/**
 * 绑车事实查询的主体投影（OTD-VSO-DSN-CR-017）
 * <p>
 * 只返回本账号匹配的主体行业务标识与角色，不读取姓名、手机号、证件、地址与授权材料。
 *
 * @author VSO Team
 */
@Data
public class BindingFactsPartyPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主体关系业务 ID
     */
    private String partyId;

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 原始角色（order_user / buyer / delivery_contact）
     */
    private String partyRole;

    /**
     * 记录版本
     */
    private Integer rowVersion;
}
