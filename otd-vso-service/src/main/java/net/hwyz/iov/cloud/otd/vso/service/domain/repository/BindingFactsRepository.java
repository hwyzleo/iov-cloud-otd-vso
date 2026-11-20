package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsDeliveryPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsOrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsPartyPo;

import java.util.List;

/**
 * 绑车事实只读仓储（OTD-VSO-DSN-CR-017）
 * <p>
 * 仅供内部事实核验查询使用：只读、无保存方法，返回最小投影。
 *
 * @author VSO Team
 */
public interface BindingFactsRepository {

    /**
     * 查询同时匹配账号与 VIN 的有效订单候选
     *
     * @param accountId 平台账号标识
     * @param vin       规范化后的 VIN
     * @param roles     角色白名单
     * @param limit     候选上限
     * @return 订单最小投影
     */
    List<BindingFactsOrderPo> findCandidateOrders(String accountId, String vin, List<String> roles, int limit);

    /**
     * 查询指定订单上本账号匹配的主体行
     *
     * @param accountId 平台账号标识
     * @param orderIds  订单业务 ID 集合
     * @param roles     角色白名单
     * @return 主体最小投影
     */
    List<BindingFactsPartyPo> findMatchedParties(String accountId, List<String> orderIds, List<String> roles);

    /**
     * 查询指定订单的全部有效交付记录
     *
     * @param orderIds 订单业务 ID 集合
     * @return 交付记录最小投影
     */
    List<BindingFactsDeliveryPo> findDeliveryRecords(List<String> orderIds);
}
