package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PurchaseBenefitsPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 购车权益 Mapper
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-14
 */
@Mapper
public interface PurchaseBenefitsMapper extends BaseDao<PurchaseBenefitsPo, Long> {
    /**
     * 查询当前有效的权益
     *
     * @param saleCode 销售代码
     * @return 权益
     */
    PurchaseBenefitsPo selectCurrentPoBySaleCode(String saleCode);
}
