package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RegistrationPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 上牌跟踪 Mapper 接口
 */
@Mapper
public interface RegistrationMapper extends BaseDao<RegistrationPo, Long> {

    RegistrationPo selectByOrderId(@Param("orderId") String orderId);

    RegistrationPo selectPoById(Long id);

    List<RegistrationPo> selectPoByMap(@Param("params") Map<String, Object> params);

    int countPoByMap(@Param("params") Map<String, Object> params);

    int insertPo(RegistrationPo entity);

    int batchInsertPo(List<RegistrationPo> entities);

    int updatePo(RegistrationPo entity);

    int logicalDeletePo(Long id);

    int physicalDeletePo(Long id);

    List<RegistrationPo> selectPoByExample(RegistrationPo example);

}
