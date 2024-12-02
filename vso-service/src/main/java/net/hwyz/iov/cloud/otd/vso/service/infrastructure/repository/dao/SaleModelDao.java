package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 销售车型 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-18
 */
@Mapper
public interface SaleModelDao extends BaseDao<SaleModelPo, Long> {

    /**
     * 通过销售编码查询销售车型信息
     *
     * @param saleCode 销售编码
     * @return 销售车型信息
     */
    SaleModelPo selectPoBySaleCode(String saleCode);

    /**
     * 批量物理删除销售车型信息
     *
     * @param ids 销售车型ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(Long[] ids);

}
