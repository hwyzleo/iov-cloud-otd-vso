package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SaleModelOptionPolicyMapper extends BaseMapper<SaleModelOptionPolicyPo> {
    List<SaleModelOptionPolicyPo> selectBySaleModelCode(@Param("saleModelCode") String saleModelCode);
    
    List<SaleModelOptionPolicyPo> selectBySaleModelCodeAndOptionCodes(
        @Param("saleModelCode") String saleModelCode,
        @Param("optionCodes") List<String> optionCodes);
    
    SaleModelOptionPolicyPo selectBySaleModelCodeAndOptionCode(
        @Param("saleModelCode") String saleModelCode,
        @Param("optionCode") String optionCode);

    int updateByIdDirect(SaleModelOptionPolicyPo po);

    /**
     * 根据 saleModelCode 物理删除所有关联策略
     *
     * @param saleModelCode 销售车型编码
     * @return 影响行数
     */
    @InterceptorIgnore(illegalSql = "true")
    @Delete("DELETE FROM vso_sale_model_option_policy WHERE sale_model_code = #{saleModelCode}")
    int physicalDeleteBySaleModelCode(@Param("saleModelCode") String saleModelCode);
}
