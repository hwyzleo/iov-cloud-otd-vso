package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigChangeRefundPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 改配退款记录 Mapper 接口
 */
@Mapper
public interface ConfigChangeRefundMapper extends BaseDao<ConfigChangeRefundPo, Long> {

    ConfigChangeRefundPo selectByRefundTaskNo(@Param("refundTaskNo") String refundTaskNo);

    ConfigChangeRefundPo selectPoById(Long id);

    List<ConfigChangeRefundPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(ConfigChangeRefundPo entity);

    int batchInsertPo(List<ConfigChangeRefundPo> entities);

    int updatePo(ConfigChangeRefundPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<ConfigChangeRefundPo> selectPoByExample(ConfigChangeRefundPo example);

    List<ConfigChangeRefundPo> selectByOrderId(@Param("orderId") String orderId);

}
