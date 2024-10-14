package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import net.hwyz.iov.cloud.tsp.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 销售车型 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-14
 */
@Mapper
public interface SaleModelDao extends BaseDao<SaleModelPo, Long> {

}
