package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MdmProjectionOptionFamilyMapper extends BaseMapper<MdmProjectionOptionFamilyPo> {
    MdmProjectionOptionFamilyPo selectByOptionFamilyCode(@Param("optionFamilyCode") String optionFamilyCode);
}
