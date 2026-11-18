package net.hwyz.iov.cloud.otd.vso.service.application.port;

/**
 * 事件载荷序列化端口（OTD-VSO-DSN-CR-012）
 * <p>
 * 应用层只负责组装事件对象，序列化实现由基础设施层提供；
 * 事件载荷必须是保留 null、时间以文本输出的不可变 JSON。
 *
 * @author VSO Team
 */
public interface EventPayloadSerializer {

    /**
     * 序列化事件载荷
     *
     * @param event 事件对象
     * @return 不可变 JSON 文本
     */
    String serialize(Object event);
}
