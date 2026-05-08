package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBuildConfigPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售车型生产配置关联 Mapper 接口
 * </p>
 *
 * @author hwyz_leo
 * @since 2026-05-05
 */
@Mapper
public interface SaleModelBuildConfigMapper extends BaseDao<SaleModelBuildConfigPo, Long> {

    int physicalDeleteByIds(@Param("ids") List<Long> ids);

    int physicalDeleteBySaleCodeAndBuildConfigCode(@Param("saleCode") String saleCode, @Param("buildConfigCode") String buildConfigCode);

    int physicalDeleteBySaleCode(@Param("saleCode") String saleCode);
}