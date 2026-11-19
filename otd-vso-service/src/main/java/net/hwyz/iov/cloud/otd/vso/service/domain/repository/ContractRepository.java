package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;

import java.util.Optional;

/**
 * 合同仓储接口（OTD-VSO-DSN-CR-014）
 * <p>
 * 对外只暴露领域对象；PO 与转换器只留在 Infrastructure。
 *
 * @author VSO Team
 */
public interface ContractRepository {

    /**
     * 保存合同（含新建与状态推进）
     */
    Contract save(Contract contract);

    /**
     * 按订单与合同类型查询当前合同（尾款准入门槛查询入口）
     */
    Optional<Contract> findByOrderIdAndType(String orderId, String contractType);

    /**
     * 按签署任务号查询合同（电子签回调定位入口）
     */
    Optional<Contract> findBySignNo(String signNo);

    /**
     * 按合同号查询合同（Mock 服务端自回调定位入口）
     */
    Optional<Contract> findByContractNo(String contractNo);

}
