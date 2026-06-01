package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
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
}
