package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * MDM Carline 投影 Mapper 接口
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Mapper
public interface MdmProjectionCarlineMapper extends BaseMapper<MdmProjectionCarlinePo> {

    @Select("SELECT * FROM mdm_projection_carline WHERE carline_code = #{carlineCode}")
    MdmProjectionCarlinePo selectByCarlineCode(@Param("carlineCode") String carlineCode);
}
