package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBaseModelPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 销售车型基础车型关联 Mapper 接口
 *
 * @author hwyz_leo
 * @since 2026-05-07
 */
@Mapper
public interface SaleModelBaseModelMapper extends BaseDao<SaleModelBaseModelPo, Long> {

    int insertPo(SaleModelBaseModelPo entity);

    int batchInsertPo(List<SaleModelBaseModelPo> entities);

    int updatePo(SaleModelBaseModelPo entity);

    SaleModelBaseModelPo selectPoById(Long id);

    List<SaleModelBaseModelPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    List<SaleModelBaseModelPo> selectPoBySaleCode(String saleCode);

    SaleModelBaseModelPo selectPoBySaleCodeAndBaseModelCode(@Param("saleCode") String saleCode, @Param("baseModelCode") String baseModelCode);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int physicalDeleteByIds(@Param("ids") List<Long> ids);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    int physicalDeleteBySaleCodeAndBaseModelCode(@Param("saleCode") String saleCode, @Param("baseModelCode") String baseModelCode);

    List<SaleModelBaseModelPo> selectPoByExample(SaleModelBaseModelPo example);

}