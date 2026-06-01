package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import java.util.List;
import java.util.Optional;

public interface SaleModelConfigPolicyRepository {
    Optional<SaleModelConfigPolicyPo> findBySaleModelCodeAndConfigCode(String saleModelCode, String configurationCode);
    List<SaleModelConfigPolicyPo> findBySaleModelCode(String saleModelCode);
    boolean existsBySaleModelCode(String saleModelCode);
    void save(SaleModelConfigPolicyPo po);
    void update(SaleModelConfigPolicyPo po);
    void reactivate(Long id, String status);
    void delete(Long id);

    /**
     * 根据 saleModelCode 批量更新状态
     * @param saleModelCode 销售车型编码
     * @param status 状态
     * @param modifyBy 修改人
     * @return 更新数量
     */
    int updateStatusBySaleModelCode(String saleModelCode, String status, String modifyBy);

    /**
     * 根据 saleModelCode 物理删除所有关联策略
     * @param saleModelCode 销售车型编码
     * @return 删除数量
     */
    int deleteBySaleModelCode(String saleModelCode);
}
