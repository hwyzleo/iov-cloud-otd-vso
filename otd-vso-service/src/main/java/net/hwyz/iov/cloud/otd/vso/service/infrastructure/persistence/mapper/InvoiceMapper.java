package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.InvoicePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 发票 Mapper 接口
 */
@Mapper
public interface InvoiceMapper extends BaseMapper<InvoicePo> {

    InvoicePo selectByOrderId(@Param("orderId") String orderId);

}
