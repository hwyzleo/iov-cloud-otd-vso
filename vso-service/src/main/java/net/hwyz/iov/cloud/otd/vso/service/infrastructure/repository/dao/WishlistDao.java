package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.WishlistPo;
import net.hwyz.iov.cloud.tsp.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 心愿单 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-10
 */
@Mapper
public interface WishlistDao extends BaseDao<WishlistPo, Long> {

}
