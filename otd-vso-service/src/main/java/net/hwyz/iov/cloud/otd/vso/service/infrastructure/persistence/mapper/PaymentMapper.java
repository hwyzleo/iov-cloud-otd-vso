package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付记录 Mapper 接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentPo> {

    PaymentPo selectByPaymentNo(@Param("paymentNo") String paymentNo);

}
