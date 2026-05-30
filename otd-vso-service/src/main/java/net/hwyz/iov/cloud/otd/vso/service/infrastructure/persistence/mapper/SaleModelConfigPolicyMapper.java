package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
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
}
