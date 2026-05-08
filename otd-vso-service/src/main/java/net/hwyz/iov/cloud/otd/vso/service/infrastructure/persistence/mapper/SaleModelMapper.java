package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 销售车型 Mapper。
 */
@Mapper
public interface SaleModelMapper extends BaseDao<SaleModelPo, Long> {

    /**
     * 通过销售编码查询销售车型信息
     *
     * @param saleCode 销售编码
     * @return 销售车型信息
     */
    SaleModelPo selectPoBySaleCode(@Param("saleCode") String saleCode);

    /**
     * 批量物理删除 PO
     *
     * @param ids ID 数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(@Param("array") Long[] ids);
}
