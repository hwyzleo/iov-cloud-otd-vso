package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBuildConfigPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    int physicalDeleteBySaleModelCodeAndBuildConfigCode(@Param("saleModelCode") String saleModelCode, @Param("buildConfigCode") String buildConfigCode);

    int physicalDeleteBySaleModelCode(@Param("saleModelCode") String saleModelCode);
}