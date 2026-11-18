package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.framework.mysql.dao.BaseDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VsoOutboxPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * VSO 出站消息 Mapper 接口
 *
 * @author VSO Team
 */
@Mapper
public interface VsoOutboxMapper extends BaseDao<VsoOutboxPo, Long> {

    /**
     * 写入待发布记录
     *
     * @param entity 记录
     * @return 影响行数
     */
    int insertPo(VsoOutboxPo entity);

    /**
     * 统计聚合的指定类型生效事件
     *
     * @param eventType   事件类型
     * @param aggregateId 聚合 ID
     * @return 记录数
     */
    int countByEventTypeAndAggregateId(@Param("eventType") String eventType,
                                       @Param("aggregateId") String aggregateId);

    /**
     * 扫描可发布记录
     *
     * @param now   当前时间
     * @param limit 批大小
     * @return 记录列表
     */
    List<VsoOutboxPo> selectPublishable(@Param("now") Date now, @Param("limit") int limit);

    /**
     * 标记已发布
     *
     * @param id 主键
     * @return 影响行数
     */
    int markPublished(@Param("id") Long id);

    /**
     * 标记可重试
     *
     * @param id            主键
     * @param retryCount    已重试次数
     * @param nextRetryTime 下次重试时间
     * @param error         脱敏错误
     * @return 影响行数
     */
    int markRetryable(@Param("id") Long id, @Param("retryCount") int retryCount,
                      @Param("nextRetryTime") Date nextRetryTime, @Param("error") String error);

    /**
     * 标记终态失败
     *
     * @param id    主键
     * @param error 脱敏错误
     * @return 影响行数
     */
    int markDead(@Param("id") Long id, @Param("error") String error);
}
