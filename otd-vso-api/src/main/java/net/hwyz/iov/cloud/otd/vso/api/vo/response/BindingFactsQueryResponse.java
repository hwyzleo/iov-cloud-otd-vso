package net.hwyz.iov.cloud.otd.vso.api.vo.response;

import lombok.*;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingDeliveryStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingEvidenceType;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingLookupStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingReasonCode;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 绑车事实查询响应（内部服务调用，OTD-VSO-DSN-CR-017）
 * <p>
 * 最小披露白名单：不回显 accountId，不包含姓名、手机号、证件、地址、授权材料、合同或支付明细，
 * 也不返回 canBind、isOwner 等资格结论。<b>查询结果不是绑定许可</b>，只证明本次读取快照。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingFactsQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 查询契约版本，初始 1（不是数据库行版本）
     */
    private Integer schemaVersion;

    /**
     * 本次读取时间的绝对时刻（ISO-8601、带 offset、固定 3 位毫秒；JSON 输出恒为 UTC {@code Z}）
     * <p>
     * 使用 {@link Instant} 而非墙上时间，避免消费方把生产者业务时区墙钟误当 UTC 落库。
     */
    private Instant checkedAt;

    /**
     * 核验结论分类
     */
    private BindingLookupStatus lookupStatus;

    /**
     * 稳定业务原因码；MATCHED 时为空
     */
    private BindingReasonCode reasonCode;

    /**
     * 订单事实；仅 MATCHED 时包含一项，其他状态为空集合
     */
    private List<Fact> facts;

    /**
     * 唯一可解释的订单事实
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fact implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 订单业务 ID
         */
        private String orderId;

        /**
         * 订单号
         */
        private String orderNo;

        /**
         * 订单状态值
         */
        private Integer orderState;

        /**
         * 订单—VIN 关联中的 VIN（规范化后大写）
         */
        private String vin;

        /**
         * 本账号在本订单实际匹配的角色集合（去重，保留原始 party_role，不折叠为“车主”）
         */
        private List<String> matchedRoles;

        /**
         * 交付核验状态
         */
        private BindingDeliveryStatus deliveryStatus;

        /**
         * 交付记录业务 ID；未交付时为空
         */
        private String deliveryRecordId;

        /**
         * 实际交付时间的绝对时刻；未交付时为空。由交付记录持久化墙上时间按业务时区换算得出
         */
        private Instant actualDeliveryTime;

        /**
         * 有界事实引用列表
         */
        private List<EvidenceRef> evidenceRefs;
    }

    /**
     * 事实引用（业务标识 + 真实持久化版本）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenceRef implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 事实类型
         */
        private BindingEvidenceType type;

        /**
         * 业务事实标识（订单 ID / 主体 partyId / 交付记录 ID）
         */
        private String id;

        /**
         * 实际持久化版本；不存在时为 null
         */
        private Integer version;
    }
}
