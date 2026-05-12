package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderMaterialPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单资料 Mapper 接口
 */
@Mapper
public interface OrderMaterialMapper extends BaseDao<OrderMaterialPo, Long> {

    OrderMaterialPo selectByOrderIdAndType(@Param("orderId") String orderId, @Param("materialType") String materialType);

    OrderMaterialPo selectPoById(Long id);

    List<OrderMaterialPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderMaterialPo entity);

    int batchInsertPo(List<OrderMaterialPo> entities);

    int updatePo(OrderMaterialPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderMaterialPo> selectPoByExample(OrderMaterialPo example);

}
