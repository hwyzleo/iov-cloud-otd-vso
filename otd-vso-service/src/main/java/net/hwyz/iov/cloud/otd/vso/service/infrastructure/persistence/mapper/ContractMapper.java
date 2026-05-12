package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ContractPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 合同 Mapper 接口
 */
@Mapper
public interface ContractMapper extends BaseDao<ContractPo, Long> {

    ContractPo selectByOrderIdAndType(@Param("orderId") String orderId, @Param("contractType") String contractType);

    ContractPo selectPoById(Long id);

    List<ContractPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(ContractPo entity);

    int batchInsertPo(List<ContractPo> entities);

    int updatePo(ContractPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<ContractPo> selectPoByExample(ContractPo example);

    List<ContractPo> selectByOrderId(@Param("orderId") String orderId);

}
