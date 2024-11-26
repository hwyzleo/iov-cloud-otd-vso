package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.PurchaseAgreementPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 购车协议 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-15
 */
@Mapper
public interface PurchaseAgreementDao extends BaseDao<PurchaseAgreementPo, Long> {

}
