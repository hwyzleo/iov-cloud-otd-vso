package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionFamilyPolicyPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SaleModelOptionFamilyPolicyMapper extends BaseMapper<SaleModelOptionFamilyPolicyPo> {
    List<SaleModelOptionFamilyPolicyPo> selectBySaleModelCode(@Param("saleModelCode") String saleModelCode);

    SaleModelOptionFamilyPolicyPo selectBySaleModelCodeAndFamilyCode(
        @Param("saleModelCode") String saleModelCode,
        @Param("optionFamilyCode") String optionFamilyCode);
}
