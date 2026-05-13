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
     * 通过销售车型代码查询销售车型信息
     *
     * @param saleModelCode 销售车型代码
     * @return 销售车型信息
     */
    SaleModelPo selectPoBySaleModelCode(@Param("saleModelCode") String saleModelCode);

    /**
     * 批量物理删除 PO
     *
     * @param ids ID 数组
     * @return 影响行数
     */
}
