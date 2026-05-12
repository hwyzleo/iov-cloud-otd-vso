package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTransformPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 小订单转正式订单转化关系表 Mapper 接口
 */
@Mapper
public interface OrderTransformMapper extends BaseDao<OrderTransformPo, Long> {

    OrderTransformPo selectPoById(Long id);

    List<OrderTransformPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderTransformPo entity);

    int batchInsertPo(List<OrderTransformPo> entities);

    int updatePo(OrderTransformPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderTransformPo> selectPoByExample(OrderTransformPo example);

}
