package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 退款记录 Mapper 接口
 */
@Mapper
public interface RefundMapper extends BaseMapper<RefundPo> {

    RefundPo selectByRefundNo(@Param("refundNo") String refundNo);

}
