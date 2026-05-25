package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 改配补款记录 Mapper 接口
 */
@Mapper
public interface SupplementaryPaymentMapper extends BaseDao<SupplementaryPaymentPo, Long> {

    SupplementaryPaymentPo selectBySupplementaryNo(@Param("supplementaryNo") String supplementaryNo);

    SupplementaryPaymentPo selectPoById(Long id);

    List<SupplementaryPaymentPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(SupplementaryPaymentPo entity);

    int batchInsertPo(List<SupplementaryPaymentPo> entities);

    int updatePo(SupplementaryPaymentPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<SupplementaryPaymentPo> selectPoByExample(SupplementaryPaymentPo example);

    List<SupplementaryPaymentPo> selectByOrderId(@Param("orderId") String orderId);

}
