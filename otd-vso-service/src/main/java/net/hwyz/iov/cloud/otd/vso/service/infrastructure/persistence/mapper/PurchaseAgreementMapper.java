package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PurchaseAgreementPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 购车协议 Mapper
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-15
 */
@Mapper
public interface PurchaseAgreementMapper extends BaseDao<PurchaseAgreementPo, Long> {

}
