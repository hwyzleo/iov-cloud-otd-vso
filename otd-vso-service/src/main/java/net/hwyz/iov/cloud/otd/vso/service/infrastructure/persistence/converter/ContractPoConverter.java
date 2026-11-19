package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ContractPo;

/**
 * 购车合同对象转换器（Domain ↔ PO，OTD-VSO-DSN-CR-014）
 * <p>
 * PO 与转换器只留在 Infrastructure，Repository 对外只暴露领域对象。
 *
 * @author VSO Team
 */
public final class ContractPoConverter {

    private ContractPoConverter() {
    }

    public static Contract toDomain(ContractPo po) {
        if (po == null) {
            return null;
        }
        Contract contract = new Contract();
        contract.setContractId(po.getContractId());
        contract.setOrderId(po.getOrderId());
        contract.setContractNo(po.getContractNo());
        contract.setContractType(po.getContractType());
        contract.setVersionNo(po.getVersionNo());
        contract.setContractStatus(parseStatus(po.getContractStatus()));
        contract.setSignMode(po.getSignMode());
        contract.setSignEffectiveRule(po.getSignEffectiveRule());
        contract.setFileUrl(po.getFileUrl());
        contract.setExternalContractNo(po.getExternalContractNo());
        contract.setSignNo(po.getSignNo());
        contract.setGenerateTime(po.getGenerateTime());
        contract.setSignTime(po.getSignTime());
        contract.setEffectiveTime(po.getEffectiveTime());
        contract.setInvalidTime(po.getInvalidTime());
        contract.setInvalidReason(po.getInvalidReason());
        return contract;
    }

    public static ContractPo fromDomain(Contract contract, Long id) {
        if (contract == null) {
            return null;
        }
        ContractPo po = new ContractPo();
        po.setId(id);
        po.setContractId(contract.getContractId());
        po.setOrderId(contract.getOrderId());
        po.setContractNo(contract.getContractNo());
        po.setContractType(contract.getContractType());
        po.setVersionNo(contract.getVersionNo());
        po.setContractStatus(contract.getContractStatus() == null ? null : contract.getContractStatus().name());
        po.setSignMode(contract.getSignMode());
        po.setSignEffectiveRule(contract.getSignEffectiveRule());
        po.setFileUrl(contract.getFileUrl());
        po.setExternalContractNo(contract.getExternalContractNo());
        po.setSignNo(contract.getSignNo());
        po.setGenerateTime(contract.getGenerateTime());
        po.setSignTime(contract.getSignTime());
        po.setEffectiveTime(contract.getEffectiveTime());
        po.setInvalidTime(contract.getInvalidTime());
        po.setInvalidReason(contract.getInvalidReason());
        return po;
    }

    private static ContractStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        for (ContractStatus value : ContractStatus.values()) {
            if (value.name().equalsIgnoreCase(status)) {
                return value;
            }
        }
        return null;
    }
}
