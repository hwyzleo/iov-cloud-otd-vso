package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryRecordPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 交付记录 Mapper 接口
 */
@Mapper
public interface DeliveryRecordMapper extends BaseDao<DeliveryRecordPo, Long> {

    DeliveryRecordPo selectByOrderId(@Param("orderId") String orderId);

    DeliveryRecordPo selectPoById(Long id);

    List<DeliveryRecordPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(DeliveryRecordPo entity);

    int batchInsertPo(List<DeliveryRecordPo> entities);

    int updatePo(DeliveryRecordPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<DeliveryRecordPo> selectPoByExample(DeliveryRecordPo example);

}
