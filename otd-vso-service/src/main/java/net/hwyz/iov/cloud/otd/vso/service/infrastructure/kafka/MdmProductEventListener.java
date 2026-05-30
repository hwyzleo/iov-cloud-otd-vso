package net.hwyz.iov.cloud.otd.vso.service.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MdmProductEventListener {

    private final MdmProjectionService mdmProjectionService;
    private final ObjectMapper objectMapper;

    /**
     * 监听 Variant 变更事件
     */
    @KafkaListener(topics = "${mdm.kafka.topics.variant:mdm.product.variant.changed}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleVariantChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 Variant 变更事件: key={}, partition={}, offset={}",
                record.key(), record.partition(), record.offset());

            JsonNode payload = objectMapper.readTree(record.value());
            String variantCode = payload.get("variantCode").asText();

            MdmProjectionVariantPo po = MdmProjectionVariantPo.builder()
                .variantCode(variantCode)
                .variantName(payload.has("variantName") ? payload.get("variantName").asText() : null)
                .modelCode(payload.has("modelCode") ? payload.get("modelCode").asText() : null)
                .modelName(payload.has("modelName") ? payload.get("modelName").asText() : null)
                .standardOptions(payload.has("standardOptions") ? payload.get("standardOptions").toString() : null)
                .status(payload.has("status") ? payload.get("status").asText() : "active")
                .build();

            mdmProjectionService.saveOrUpdateVariant(po);
            log.info("Variant 投影更新完成: {}", variantCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 Variant 变更事件失败", e);
        }
    }

    /**
     * 监听 Configuration 变更事件
     */
    @KafkaListener(topics = "${mdm.kafka.topics.configuration:mdm.product.configuration.changed}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleConfigurationChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 Configuration 变更事件: key={}, partition={}, offset={}",
                record.key(), record.partition(), record.offset());

            JsonNode payload = objectMapper.readTree(record.value());
            String configurationCode = payload.get("configurationCode").asText();

            MdmProjectionConfigurationPo po = MdmProjectionConfigurationPo.builder()
                .configurationCode(configurationCode)
                .variantCode(payload.has("variantCode") ? payload.get("variantCode").asText() : null)
                .optionCodes(payload.has("optionCodes") ? payload.get("optionCodes").toString() : null)
                .guidePrice(payload.has("guidePrice") ? payload.get("guidePrice").decimalValue() : null)
                .status(payload.has("status") ? payload.get("status").asText() : "active")
                .build();

            mdmProjectionService.saveOrUpdateConfiguration(po);
            log.info("Configuration 投影更新完成: {}", configurationCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 Configuration 变更事件失败", e);
        }
    }

    /**
     * 监听 OptionCode 变更事件
     */
    @KafkaListener(topics = "${mdm.kafka.topics.option:mdm.product.option.changed}", groupId = "${mdm.kafka.group-id:vso-mdm-projection}")
    public void handleOptionChanged(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("收到 OptionCode 变更事件: key={}, partition={}, offset={}",
                record.key(), record.partition(), record.offset());

            JsonNode payload = objectMapper.readTree(record.value());
            String optionCode = payload.get("optionCode").asText();

            MdmProjectionOptionPo po = MdmProjectionOptionPo.builder()
                .optionCode(optionCode)
                .optionFamilyCode(payload.has("optionFamilyCode") ? payload.get("optionFamilyCode").asText() : null)
                .optionName(payload.has("optionName") ? payload.get("optionName").asText() : null)
                .mutexWith(payload.has("mutexWith") ? payload.get("mutexWith").toString() : null)
                .bundleWith(payload.has("bundleWith") ? payload.get("bundleWith").toString() : null)
                .status(payload.has("status") ? payload.get("status").asText() : "active")
                .build();

            mdmProjectionService.saveOrUpdateOption(po);
            log.info("OptionCode 投影更新完成: {}", optionCode);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理 OptionCode 变更事件失败", e);
        }
    }
}
