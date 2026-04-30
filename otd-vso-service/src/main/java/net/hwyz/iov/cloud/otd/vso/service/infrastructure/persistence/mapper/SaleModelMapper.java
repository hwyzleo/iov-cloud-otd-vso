package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

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
     * 根据ID查询 PO
     *
     * @param id ID
     * @return 销售车型 PO
     */
    SaleModelPo selectPoById(Long id);

    /**
     * 根据 Map 查询 PO 列表
     *
     * @param params 查询条件
     * @return 销售车型 PO 列表
     */
    java.util.List<SaleModelPo> selectPoByMap(@Param("params") Map<String, Object> params);

    /**
     * 根据 Map 统计记录数
     *
     * @param params 查询条件
     * @return 记录数
     */
    int countPoByMap(@Param("params") Map<String, Object> params);

    /**
     * 插入 PO
     *
     * @param entity 实体
     * @return 影响行数
     */
    int insertPo(SaleModelPo entity);

    /**
     * 批量插入 PO
     *
     * @param entities 实体集合
     * @return 影响行数
     */
    int batchInsertPo(java.util.List<SaleModelPo> entities);

    /**
     * 更新 PO
     *
     * @param entity 实体
     * @return 影响行数
     */
    int updatePo(SaleModelPo entity);

    /**
     * 逻辑删除 PO
     *
     * @param id ID
     * @return 影响行数
     */
    int logicalDeletePo(Long id);

    /**
     * 物理删除 PO
     *
     * @param id ID
     * @return 影响行数
     */
    int physicalDeletePo(Long id);

    /**
     * 批量物理删除 PO
     *
     * @param ids ID 数组
     * @return 影响行数
     */
    int batchPhysicalDeletePo(@Param("array") Long[] ids);

    /**
     * 根据示例查询 PO 列表
     *
     * @param example 示例对象
     * @return 销售车型 PO 列表
     */
    java.util.List<SaleModelPo> selectPoByExample(SaleModelPo example);
    
}
