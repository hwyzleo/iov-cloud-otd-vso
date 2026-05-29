package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MdmProjectionConfigurationMapper extends BaseMapper<MdmProjectionConfigurationPo> {
    MdmProjectionConfigurationPo selectByConfigurationCode(@Param("configurationCode") String configurationCode);
    
    List<MdmProjectionConfigurationPo> selectByVariantCode(@Param("variantCode") String variantCode);
}
