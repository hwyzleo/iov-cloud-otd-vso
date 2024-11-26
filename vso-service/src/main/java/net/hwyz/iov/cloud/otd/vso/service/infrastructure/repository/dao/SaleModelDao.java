package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 销售车型 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-18
 */
@Mapper
public interface SaleModelDao extends BaseDao<SaleModelPo, Long> {

}
