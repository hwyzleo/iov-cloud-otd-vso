package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.InvoicePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 发票 Mapper 接口
 */
@Mapper
public interface InvoiceMapper extends BaseDao<InvoicePo, Long> {

    InvoicePo selectByOrderId(@Param("orderId") String orderId);

    InvoicePo selectPoById(Long id);

    List<InvoicePo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(InvoicePo entity);

    int batchInsertPo(List<InvoicePo> entities);

    int updatePo(InvoicePo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<InvoicePo> selectPoByExample(InvoicePo example);

}
