package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MDM Model 投影 Mapper 接口
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Mapper
public interface MdmProjectionModelMapper extends BaseMapper<MdmProjectionModelPo> {

    @Select("SELECT * FROM mdm_projection_model WHERE model_code = #{modelCode}")
    MdmProjectionModelPo selectByModelCode(@Param("modelCode") String modelCode);

    @Select("SELECT * FROM mdm_projection_model WHERE carline_code = #{carlineCode}")
    List<MdmProjectionModelPo> selectByCarlineCode(@Param("carlineCode") String carlineCode);
}
