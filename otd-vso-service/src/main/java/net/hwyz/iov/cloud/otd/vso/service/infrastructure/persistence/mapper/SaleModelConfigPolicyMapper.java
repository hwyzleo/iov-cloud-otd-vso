package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

@Mapper
public interface SaleModelConfigPolicyMapper extends BaseMapper<SaleModelConfigPolicyPo> {
    List<SaleModelConfigPolicyPo> selectBySaleModelCode(@Param("saleModelCode") String saleModelCode);
    
    SaleModelConfigPolicyPo selectBySaleModelCodeAndConfigCode(
        @Param("saleModelCode") String saleModelCode,
        @Param("configurationCode") String configurationCode);

    int reactivateById(@Param("id") Long id, @Param("status") String status, @Param("modifyTime") Date modifyTime);

    /**
     * 根据 saleModelCode 物理删除所有关联策略
     *
     * @param saleModelCode 销售车型编码
     * @return 影响行数
     */
    @InterceptorIgnore(illegalSql = "true")
    @Delete("DELETE FROM vso_sale_model_config_policy WHERE sale_model_code = #{saleModelCode}")
    int physicalDeleteBySaleModelCode(@Param("saleModelCode") String saleModelCode);
}
