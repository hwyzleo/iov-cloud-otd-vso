package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MdmProjectionOptionMapper extends BaseMapper<MdmProjectionOptionPo> {
    MdmProjectionOptionPo selectByOptionCode(@Param("optionCode") String optionCode);

    List<MdmProjectionOptionPo> selectByOptionFamilyCode(@Param("optionFamilyCode") String optionFamilyCode);
}
