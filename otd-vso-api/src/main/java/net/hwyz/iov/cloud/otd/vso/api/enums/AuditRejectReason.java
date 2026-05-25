package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditRejectReason {

    INCOMPLETE_INFO("资料不全"),
    INCORRECT_INFO("信息有误"),
    RISK_BLOCK("风险拦截"),
    DUPLICATE_ORDER("重复订单"),
    OTHER("其他");

    private final String description;
}
