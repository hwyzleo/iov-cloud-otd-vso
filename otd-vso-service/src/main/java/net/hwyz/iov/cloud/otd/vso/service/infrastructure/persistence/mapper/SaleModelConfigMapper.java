package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 销售车型配置 Mapper。
 */
@Mapper
public interface SaleModelConfigMapper extends BaseDao<SaleModelConfigPo, Long> {

    /**
     * 批量物理删除销售车型配置信息
     *
     * @param saleCode 销售代码
     * @param ids      销售车型ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(@Param("saleModelCode") String saleModelCode, @Param("ids") Long[] ids);

    /**
     * 根据销售车型代码查询配置
     *
     * @param saleModelCode 销售车型代码
     * @return 配置列表
     */
    java.util.List<net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo> selectPoBySaleModelCode(@Param("saleModelCode") String saleModelCode);

    /**
     * 根据ID和销售车型代码查询配置
     *
     * @param id       ID
     * @param saleModelCode 销售车型代码
     * @return 配置 PO
     */
    net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo selectPoByIdAndSaleModelCode(@Param("id") Long id, @Param("saleModelCode") String saleModelCode);
}
