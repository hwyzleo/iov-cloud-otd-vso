package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface SaleModelBaseModelMapper extends BaseMapper<SaleModelBaseModelPo> {

    int insertPo(SaleModelBaseModelPo entity);

    int updatePo(SaleModelBaseModelPo entity);

    SaleModelBaseModelPo selectPoById(Long id);

    List<SaleModelBaseModelPo> selectPoBySaleCode(String saleCode);

    SaleModelBaseModelPo selectPoBySaleCodeAndBaseModelCode(@Param("saleCode") String saleCode, @Param("baseModelCode") String baseModelCode);

    int physicalDeleteByIds(@Param("ids") List<Long> ids);

    int physicalDeleteBySaleCodeAndBaseModelCode(@Param("saleCode") String saleCode, @Param("baseModelCode") String baseModelCode);
}