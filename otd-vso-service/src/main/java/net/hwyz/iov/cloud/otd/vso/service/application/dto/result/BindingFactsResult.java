package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.*;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingDeliveryStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingEvidenceType;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingLookupStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingReasonCode;

import java.time.Instant;
import java.util.List;

/**
 * 绑车事实查询结果（OTD-VSO-DSN-CR-017）
 * <p>
 * 应用层只产出本地已提交事实的最小化投影与分类结论，不包含任何资格判断。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingFactsResult {

    /**
     * 查询契约版本
     */
    private Integer schemaVersion;

    /**
     * 本次读取时间的绝对时刻
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
     * 订单事实；仅 MATCHED 时包含一项
     */
    private List<Fact> facts;

    /**
     * 唯一可解释的订单事实
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fact {

        private String orderId;

        private String orderNo;

        private Integer orderState;

        private String vin;

        private List<String> matchedRoles;

        private BindingDeliveryStatus deliveryStatus;

        private String deliveryRecordId;

        private Instant actualDeliveryTime;

        private List<EvidenceRef> evidenceRefs;
    }

    /**
     * 事实引用
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenceRef {

        private BindingEvidenceType type;

        private String id;

        private Integer version;
    }
}
