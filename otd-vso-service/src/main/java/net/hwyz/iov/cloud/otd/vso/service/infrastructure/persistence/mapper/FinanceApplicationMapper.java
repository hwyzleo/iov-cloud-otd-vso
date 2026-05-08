package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.FinanceApplicationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 金融申请 Mapper 接口
 */
@Mapper
public interface FinanceApplicationMapper extends BaseDao<FinanceApplicationPo, Long> {

    FinanceApplicationPo selectByOrderId(@Param("orderId") String orderId);

    FinanceApplicationPo selectPoById(Long id);

    List<FinanceApplicationPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(FinanceApplicationPo entity);

    int batchInsertPo(List<FinanceApplicationPo> entities);

    int updatePo(FinanceApplicationPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    List<FinanceApplicationPo> selectPoByExample(FinanceApplicationPo example);

}
