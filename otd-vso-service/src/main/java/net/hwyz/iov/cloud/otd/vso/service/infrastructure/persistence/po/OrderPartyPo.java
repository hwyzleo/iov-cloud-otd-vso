package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单客户与购车人信息表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_order_party")
public class OrderPartyPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主体关系业务 ID
     */
    @TableField("party_id")
    private String partyId;

    /**
     * 订单业务 ID
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 角色
     */
    @TableField("party_role")
    private String partyRole;

    /**
     * 平台用户 ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 手机号密文
     */
    @TableField("mobile_encrypted")
    private String mobileEncrypted;

    /**
     * 手机号哈希
     */
    @TableField("mobile_hash")
    private String mobileHash;

    /**
     * 身份证号密文
     */
    @TableField("id_no_encrypted")
    private String idNoEncrypted;

    /**
     * 身份证号哈希
     */
    @TableField("id_no_hash")
    private String idNoHash;

    /**
     * 与购车人关系
     */
    @TableField("relation_to_buyer")
    private String relationToBuyer;

    /**
     * 地址密文
     */
    @TableField("address_encrypted")
    private String addressEncrypted;

    /**
     * 是否具备授权
     */
    @TableField("authorized_flag")
    private Integer authorizedFlag;

    /**
     * 授权依据类型
     */
    @TableField("authorized_proof_type")
    private String authorizedProofType;

    /**
     * 授权材料地址
     */
    @TableField("authorized_proof_url")
    private String authorizedProofUrl;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 修改时间
     */
    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;

    /**
     * 修改者
     */
    @TableField(value = "modify_by", fill = FieldFill.INSERT_UPDATE)
    private Long modifyBy;

    /**
     * 记录版本
     */
    @TableField(value = "row_version", fill = FieldFill.INSERT)
    private Integer rowVersion;

    /**
     * 是否有效
     */
    @TableField(value = "row_valid", fill = FieldFill.INSERT)
    private Integer rowValid;

}
