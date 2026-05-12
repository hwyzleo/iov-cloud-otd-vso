package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderShadowDeletePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 物理删除审计影子记录表 Mapper 接口
 */
@Mapper
public interface OrderShadowDeleteMapper extends BaseDao<OrderShadowDeletePo, Long> {

    OrderShadowDeletePo selectPoById(Long id);

    List<OrderShadowDeletePo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderShadowDeletePo entity);

    int batchInsertPo(List<OrderShadowDeletePo> entities);

    int updatePo(OrderShadowDeletePo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderShadowDeletePo> selectPoByExample(OrderShadowDeletePo example);

}
