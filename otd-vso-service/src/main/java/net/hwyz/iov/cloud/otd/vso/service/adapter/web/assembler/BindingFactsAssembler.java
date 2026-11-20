package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.api.vo.response.BindingFactsQueryResponse;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.BindingFactsResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 绑车事实查询响应装配（OTD-VSO-DSN-CR-017）
 * <p>
 * 只做字段搬运，不新增任何资格字段：不回显 accountId，不输出客户资料。
 *
 * @author VSO Team
 */
public final class BindingFactsAssembler {

    private BindingFactsAssembler() {
    }

    /**
     * 应用结果 → API 响应
     *
     * @param result 应用层结果
     * @return API 响应
     */
    public static BindingFactsQueryResponse toResponse(BindingFactsResult result) {
        if (result == null) {
            return null;
        }
        return BindingFactsQueryResponse.builder()
                .schemaVersion(result.getSchemaVersion())
                .checkedAt(result.getCheckedAt())
                .lookupStatus(result.getLookupStatus())
                .reasonCode(result.getReasonCode())
                .facts(toFacts(result.getFacts()))
                .build();
    }

    private static List<BindingFactsQueryResponse.Fact> toFacts(List<BindingFactsResult.Fact> facts) {
        if (facts == null || facts.isEmpty()) {
            return List.of();
        }
        return facts.stream().map(BindingFactsAssembler::toFact).collect(Collectors.toList());
    }

    private static BindingFactsQueryResponse.Fact toFact(BindingFactsResult.Fact fact) {
        List<BindingFactsQueryResponse.EvidenceRef> evidenceRefs = fact.getEvidenceRefs() == null
                ? List.of()
                : fact.getEvidenceRefs().stream()
                .map(ref -> BindingFactsQueryResponse.EvidenceRef.builder()
                        .type(ref.getType())
                        .id(ref.getId())
                        .version(ref.getVersion())
                        .build())
                .collect(Collectors.toList());
        return BindingFactsQueryResponse.Fact.builder()
                .orderId(fact.getOrderId())
                .orderNo(fact.getOrderNo())
                .orderState(fact.getOrderState())
                .vin(fact.getVin())
                .matchedRoles(fact.getMatchedRoles())
                .deliveryStatus(fact.getDeliveryStatus())
                .deliveryRecordId(fact.getDeliveryRecordId())
                .actualDeliveryTime(fact.getActualDeliveryTime())
                .evidenceRefs(evidenceRefs)
                .build();
    }
}
