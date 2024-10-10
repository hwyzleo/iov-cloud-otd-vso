package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.WishlistDetailPo;
import net.hwyz.iov.cloud.tsp.framework.mysql.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 心愿单详情 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-10
 */
@Mapper
public interface WishlistDetailDao extends BaseDao<WishlistDetailPo, Long> {

    /**
     * 物理删除心愿单详情
     *
     * @param wishlistId 心愿单ID
     * @return 影响记录数
     */
    int physicalDeletePoByWishlistId(Long wishlistId);

}
