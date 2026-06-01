package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import java.util.List;
import java.util.Optional;

public interface SaleModelOptionPolicyRepository {
    Optional<SaleModelOptionPolicyPo> findById(Long id);
    Optional<SaleModelOptionPolicyPo> findBySaleModelCodeAndOptionCode(String saleModelCode, String optionCode);
    List<SaleModelOptionPolicyPo> findBySaleModelCode(String saleModelCode);
    List<SaleModelOptionPolicyPo> findBySaleModelCodeAndOptionCodes(String saleModelCode, List<String> optionCodes);
    void save(SaleModelOptionPolicyPo po);
    void update(SaleModelOptionPolicyPo po);
    void delete(Long id);

    /**
     * 根据 saleModelCode 批量更新销售状态
     * @param saleModelCode 销售车型编码
     * @param saleStatus 销售状态
     * @param modifyBy 修改人
     * @return 更新数量
     */
    int updateSaleStatusBySaleModelCode(String saleModelCode, String saleStatus, String modifyBy);

    /**
     * 根据 saleModelCode 物理删除所有关联策略
     * @param saleModelCode 销售车型编码
     * @return 删除数量
     */
    int deleteBySaleModelCode(String saleModelCode);
}
