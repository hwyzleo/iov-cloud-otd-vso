package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVersionDiffPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单版本差异表 Mapper 接口
 */
@Mapper
public interface OrderVersionDiffMapper extends BaseDao<OrderVersionDiffPo, Long> {

    OrderVersionDiffPo selectPoById(Long id);

    List<OrderVersionDiffPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderVersionDiffPo entity);

    int batchInsertPo(List<OrderVersionDiffPo> entities);

    int updatePo(OrderVersionDiffPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<OrderVersionDiffPo> selectPoByExample(OrderVersionDiffPo example);

}
