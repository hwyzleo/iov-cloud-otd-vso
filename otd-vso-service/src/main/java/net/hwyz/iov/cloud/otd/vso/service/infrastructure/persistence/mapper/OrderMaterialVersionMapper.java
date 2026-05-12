package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderMaterialVersionPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单资料版本表 Mapper 接口
 */
@Mapper
public interface OrderMaterialVersionMapper extends BaseDao<OrderMaterialVersionPo, Long> {

    OrderMaterialVersionPo selectPoById(Long id);

    List<OrderMaterialVersionPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(OrderMaterialVersionPo entity);

    int batchInsertPo(List<OrderMaterialVersionPo> entities);

    int updatePo(OrderMaterialVersionPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<OrderMaterialVersionPo> selectPoByExample(OrderMaterialVersionPo example);

}
