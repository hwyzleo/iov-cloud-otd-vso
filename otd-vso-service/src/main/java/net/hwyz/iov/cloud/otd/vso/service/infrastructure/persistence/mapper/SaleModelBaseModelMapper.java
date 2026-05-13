package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBaseModelPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 销售车型基础车型关联 Mapper 接口
 *
 * @author hwyz_leo
 * @since 2026-05-07
 */
@Mapper
public interface SaleModelBaseModelMapper extends BaseDao<SaleModelBaseModelPo, Long> {

    List<SaleModelBaseModelPo> selectPoBySaleModelCode(String saleModelCode);

    SaleModelBaseModelPo selectPoBySaleModelCodeAndBaseModelCode(@Param("saleModelCode") String saleModelCode, @Param("baseModelCode") String baseModelCode);

    int physicalDeleteByIds(@Param("ids") List<Long> ids);

    int physicalDeleteBySaleModelCodeAndBaseModelCode(@Param("saleModelCode") String saleModelCode, @Param("baseModelCode") String baseModelCode);
}