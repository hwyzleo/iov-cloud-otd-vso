package net.hwyz.iov.cloud.otd.vso.service.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import net.hwyz.iov.cloud.otd.vso.service.application.port.EventPayloadSerializer;
import org.springframework.stereotype.Component;

/**
 * 出站事件载荷序列化实现（OTD-VSO-DSN-CR-012）
 * <p>
 * 使用独立的静态 {@link ObjectMapper}，<b>不注册为 Spring Bean</b>：
 * 额外注册 {@code ObjectMapper} Bean 会关闭 Boot 的条件化主 mapper，导致框架 Jackson 启动探针
 * 按错误的 mapper 校验（FW-WEB-DSN-CR-003）并影响全局接口的时间输出。
 *
 * @author VSO Team
 */
@Component
public class JacksonEventPayloadSerializer implements EventPayloadSerializer {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.ALWAYS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Override
    public String serialize(Object event) {
        try {
            return MAPPER.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("出站事件序列化失败", e);
        }
    }
}
