package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsDeliveryPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsOrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsPartyPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 绑车事实只读查询 Mapper（OTD-VSO-DSN-CR-017）
 * <p>
 * 先限定账号与 VIN 再读取最小字段，不做全表加载后内存筛选；
 * 候选上限由调用方显式传入，用于在数据库侧探测歧义而不截断噪音。
 *
 * @author VSO Team
 */
@Mapper
public interface BindingFactsMapper {

    /**
     * 查询同时匹配账号与 VIN 的有效订单候选（最多 limit 条）
     *
     * @param accountId 平台账号标识
     * @param vin       规范化后的 VIN
     * @param roles     构成关联的角色白名单
     * @param limit     候选上限
     * @return 订单最小投影
     */
    List<BindingFactsOrderPo> selectCandidateOrders(@Param("accountId") String accountId,
                                                    @Param("vin") String vin,
                                                    @Param("roles") List<String> roles,
                                                    @Param("limit") int limit);

    /**
     * 查询指定订单上本账号匹配的主体行（集合查询，避免 N+1）
     *
     * @param accountId 平台账号标识
     * @param orderIds  订单业务 ID 集合
     * @param roles     角色白名单
     * @return 主体最小投影
     */
    List<BindingFactsPartyPo> selectMatchedParties(@Param("accountId") String accountId,
                                                   @Param("orderIds") List<String> orderIds,
                                                   @Param("roles") List<String> roles);

    /**
     * 查询指定订单的全部有效交付记录（集合查询，用于识别多条互斥交付事实）
     *
     * @param orderIds 订单业务 ID 集合
     * @return 交付记录最小投影
     */
    List<BindingFactsDeliveryPo> selectDeliveryRecords(@Param("orderIds") List<String> orderIds);
}
