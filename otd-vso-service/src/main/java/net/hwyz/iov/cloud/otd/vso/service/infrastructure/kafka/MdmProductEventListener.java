package net.hwyz.iov.cloud.otd.vso.service.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * MDM 产品主数据事件监听器
 * <p>
 * Topic 与消息内容以 EDD-MDM 的 Kafka Topic 目录及 {@code OutboxRelayScheduler} 实际投递为准：
 * <ul>
 *     <li>Topic：{@code mdm.car-line} / {@code mdm.model} / {@code mdm.variant} /
 *     {@code mdm.configuration} / {@code mdm.option-code} / {@code mdm.option-family}</li>
 *     <li>消息 value：MDM 聚合根序列化后的 JSON（如 {@code Model}/{@code Variant}），
 *     根字段为 {@code code}、{@code name}、{@code nameLocal}、{@code status} 等，
 *     不再使用 {@code variantCode}、{@code carlineCode}、{@code *.changed} 旧契约</li>
 *     <li>消息 key：MDM 聚合根 code（同时作为缺失 code 时的兜底）</li>
 * </ul>
 * 为了兼容可能存在的 envelope（{@code {eventType, entityId, payload:{...}}}），解析时优先使用
 * {@code payload} 子节点，缺失时回退到消息根节点。
 * <p>
 * MDM 事件载荷不包含 VSO 侧扩展字段（如 Configuration 的 optionCodes/guidePrice、Model 的
 * variantCodes、CarLine 的 modelCodes、Variant 的 standardOptions），这些字段在消息中缺失时
 * 保持为 null，由 MyBatis-Plus 的 NOT_NULL 更新策略保证不会覆盖既有投影数据。
 *
 * @author hwyz_leo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MdmProductEventListener {

    private static final String DEFAULT_STATUS = "active";

    private final MdmProjectionService mdmProjectionService;
    private final ObjectMapper objectMapper;

    /**
     * 监听 Variant 变更事件（mdm.variant）
     */
    @KafkaListener(topics = "${mdm.kafka.topics.variant:mdm.variant}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleVariantChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 Variant 变更事件: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

            JsonNode payload = readPayload(record);
            String variantCode = resolveCode(payload, record, "code", "variantCode");
            String modelCode = text(payload, "modelCode");
            String modelName = text(payload, "modelName");
            if (modelName == null && modelCode != null) {
                modelName = mdmProjectionService.getModelOptional(modelCode)
                    .map(MdmProjectionModelPo::getModelName)
                    .orElse(null);
            }

            MdmProjectionVariantPo po = MdmProjectionVariantPo.builder()
                .variantCode(variantCode)
                .variantName(text(payload, "name", "variantName", "nameLocal"))
                .modelCode(modelCode)
                .modelName(modelName)
                .standardOptions(json(payload, "standardOptions"))
                .status(status(payload))
                .build();

            mdmProjectionService.saveOrUpdateVariant(po);
            log.info("Variant 投影更新完成: {}", variantCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 Variant 变更事件失败: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset(), e);
        }
    }

    /**
     * 监听 Configuration 变更事件（mdm.configuration）
     */
    @KafkaListener(topics = "${mdm.kafka.topics.configuration:mdm.configuration}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleConfigurationChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 Configuration 变更事件: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

            JsonNode payload = readPayload(record);
            String configurationCode = resolveCode(payload, record, "code", "configurationCode");

            MdmProjectionConfigurationPo po = MdmProjectionConfigurationPo.builder()
                .configurationCode(configurationCode)
                .variantCode(text(payload, "variantCode"))
                .optionCodes(json(payload, "optionCodes"))
                .guidePrice(payload.hasNonNull("guidePrice") ? payload.get("guidePrice").decimalValue() : null)
                .status(status(payload))
                .build();

            mdmProjectionService.saveOrUpdateConfiguration(po);
            log.info("Configuration 投影更新完成: {}", configurationCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 Configuration 变更事件失败: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset(), e);
        }
    }

    /**
     * 监听 OptionCode 变更事件（mdm.option-code）
     */
    @KafkaListener(topics = "${mdm.kafka.topics.option-code:mdm.option-code}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleOptionChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 OptionCode 变更事件: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

            JsonNode payload = readPayload(record);
            String optionCode = resolveCode(payload, record, "code", "optionCode");

            MdmProjectionOptionPo po = MdmProjectionOptionPo.builder()
                .optionCode(optionCode)
                .optionFamilyCode(text(payload, "optionFamilyCode"))
                .optionName(text(payload, "name", "optionName", "nameLocal"))
                .mutexWith(json(payload, "mutexWith"))
                .bundleWith(json(payload, "bundleWith"))
                .status(status(payload))
                .build();

            mdmProjectionService.saveOrUpdateOption(po);
            log.info("OptionCode 投影更新完成: {}", optionCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 OptionCode 变更事件失败: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset(), e);
        }
    }

    /**
     * 监听 OptionFamily 变更事件（mdm.option-family）
     */
    @KafkaListener(topics = "${mdm.kafka.topics.option-family:mdm.option-family}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleOptionFamilyChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 OptionFamily 变更事件: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

            JsonNode payload = readPayload(record);
            String optionFamilyCode = resolveCode(payload, record, "code", "optionFamilyCode");

            MdmProjectionOptionFamilyPo po = MdmProjectionOptionFamilyPo.builder()
                .optionFamilyCode(optionFamilyCode)
                .optionFamilyName(text(payload, "name", "optionFamilyName", "nameLocal"))
                .status(status(payload))
                .build();

            mdmProjectionService.saveOrUpdateOptionFamily(po);
            log.info("OptionFamily 投影更新完成: {}", optionFamilyCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 OptionFamily 变更事件失败: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset(), e);
        }
    }

    /**
     * 监听 Model 变更事件（mdm.model）
     */
    @KafkaListener(topics = "${mdm.kafka.topics.model:mdm.model}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleModelChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 Model 变更事件: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

            JsonNode payload = readPayload(record);
            String modelCode = resolveCode(payload, record, "code", "modelCode");

            MdmProjectionModelPo po = MdmProjectionModelPo.builder()
                .modelCode(modelCode)
                .modelName(text(payload, "name", "modelName", "nameLocal"))
                .carlineCode(text(payload, "carLineCode", "carlineCode"))
                .variantCodes(json(payload, "variantCodes"))
                .status(status(payload))
                .build();

            mdmProjectionService.saveOrUpdateModel(po);
            log.info("Model 投影更新完成: {}", modelCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 Model 变更事件失败: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset(), e);
        }
    }

    /**
     * 监听 CarLine 变更事件（mdm.car-line）
     */
    @KafkaListener(topics = "${mdm.kafka.topics.carline:mdm.car-line}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleCarlineChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 CarLine 变更事件: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

            JsonNode payload = readPayload(record);
            String carlineCode = resolveCode(payload, record, "code", "carLineCode", "carlineCode");

            MdmProjectionCarlinePo po = MdmProjectionCarlinePo.builder()
                .carlineCode(carlineCode)
                .carlineName(text(payload, "name", "carlineName", "nameLocal"))
                .modelCodes(json(payload, "modelCodes"))
                .status(status(payload))
                .build();

            mdmProjectionService.saveOrUpdateCarline(po);
            log.info("CarLine 投影更新完成: {}", carlineCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 CarLine 变更事件失败: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset(), e);
        }
    }

    /**
     * 读取事件载荷：优先取 envelope 的 payload 子节点，其次取消息根节点。
     */
    private JsonNode readPayload(ConsumerRecord<String, String> record) throws Exception {
        if (record.value() == null || record.value().isEmpty()) {
            throw new IllegalArgumentException("消息体为空");
        }
        JsonNode root = objectMapper.readTree(record.value());
        if (root == null || root.isNull() || !root.isObject()) {
            throw new IllegalArgumentException("消息体不是合法的 JSON 对象");
        }
        JsonNode payload = root.get("payload");
        return payload != null && payload.isObject() ? payload : root;
    }

    /**
     * 解析业务主键：优先取载荷中的 code 类字段，缺失时回退到 Kafka 消息 key。
     */
    private String resolveCode(JsonNode payload, ConsumerRecord<String, String> record, String... fields) {
        String code = text(payload, fields);
        if (code == null) {
            code = record.key();
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("消息缺少业务主键 code，key=" + record.key());
        }
        return code;
    }

    /**
     * 读取状态字段，缺失时回退到默认状态。
     */
    private String status(JsonNode payload) {
        String status = text(payload, "status");
        return status != null ? status : DEFAULT_STATUS;
    }

    /**
     * 按顺序读取第一个非空文本字段。
     */
    private String text(JsonNode payload, String... fields) {
        for (String field : fields) {
            if (payload.hasNonNull(field)) {
                String value = payload.get(field).asText();
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * 读取 JSON 节点并序列化为字符串，缺失时返回 null（保持既有投影字段不被覆盖）。
     */
    private String json(JsonNode payload, String field) {
        return payload.hasNonNull(field) ? payload.get(field).toString() : null;
    }
}
