package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.WishlistPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 心愿单 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface WishlistMapper extends BaseDao<WishlistPo, Long> {

    WishlistPo selectByWishlistId(String wishlistId);

    WishlistPo selectByWishlistIdAndUserId(@Param("wishlistId") String wishlistId, @Param("userId") String userId);

    List<WishlistPo> selectByUserId(@Param("userId") String userId);

    List<WishlistPo> selectPoByMap(Map<String, Object> params);

    void deleteByUserId(@Param("userId") String userId);

    long countByUserId(@Param("userId") String userId);

    WishlistPo selectByUniqueKey(@Param("userId") String userId,
                                 @Param("saleModelCode") String saleModelCode,
                                 @Param("modelCode") String modelCode,
                                 @Param("variantCode") String variantCode,
                                 @Param("configurationCode") String configurationCode,
                                 @Param("optionCodesHash") String optionCodesHash);

    WishlistPo selectByUniqueKeyExcluding(@Param("userId") String userId,
                                          @Param("saleModelCode") String saleModelCode,
                                          @Param("modelCode") String modelCode,
                                          @Param("variantCode") String variantCode,
                                          @Param("configurationCode") String configurationCode,
                                          @Param("optionCodesHash") String optionCodesHash,
                                          @Param("excludeWishlistId") String excludeWishlistId);

    List<WishlistPo> selectBySaleModelCode(@Param("saleModelCode") String saleModelCode);

}
