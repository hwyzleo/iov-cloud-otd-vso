package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MdmProjectionVariantMapper extends BaseMapper<MdmProjectionVariantPo> {
    MdmProjectionVariantPo selectByVariantCode(@Param("variantCode") String variantCode);
}
