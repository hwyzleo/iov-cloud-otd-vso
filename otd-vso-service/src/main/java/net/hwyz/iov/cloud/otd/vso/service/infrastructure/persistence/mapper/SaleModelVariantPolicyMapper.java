package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Variant 销售策略 Mapper 接口
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Mapper
public interface SaleModelVariantPolicyMapper extends BaseMapper<SaleModelVariantPolicyPo> {

    /**
     * 根据 saleModelCode 物理删除所有关联策略
     *
     * @param saleModelCode 销售车型编码
     * @return 影响行数
     */
    @InterceptorIgnore(illegalSql = "true")
    @Delete("DELETE FROM vso_sale_model_variant_policy WHERE sale_model_code = #{saleModelCode}")
    int physicalDeleteBySaleModelCode(@Param("saleModelCode") String saleModelCode);
}
