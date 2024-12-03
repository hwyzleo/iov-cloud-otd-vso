package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 销售车型配置 DAO
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-11
 */
@Mapper
public interface SaleModelConfigDao extends BaseDao<SaleModelConfigPo, Long> {

    /**
     * 批量物理删除销售车型配置信息
     *
     * @param saleCode 销售代码
     * @param ids      销售车型ID数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(String saleCode, Long[] ids);

}
