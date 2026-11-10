package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 退款记录 Mapper 接口
 */
@Mapper
public interface RefundMapper extends BaseDao<RefundPo, Long> {

    RefundPo selectByRefundNo(@Param("refundNo") String refundNo);

    RefundPo selectPoById(Long id);

    /**
     * 查询订单当前活动退款主单（pending / processing / failed）
     */
    RefundPo selectActiveByOrderId(@Param("orderId") String orderId);

    List<RefundPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(RefundPo entity);

    int batchInsertPo(List<RefundPo> entities);

    int updatePo(RefundPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<RefundPo> selectPoByExample(RefundPo example);

    List<RefundPo> selectByOrderId(@Param("orderId") String orderId);

}
