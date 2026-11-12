package net.hwyz.iov.cloud.otd.vso.service.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("MdmProductEventListener - MDM 新 Topic 与载荷契约测试")
class MdmProductEventListenerTest {

    private MdmProjectionService projectionService;
    private MdmProductEventListener listener;
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        projectionService = mock(MdmProjectionService.class);
        acknowledgment = mock(Acknowledgment.class);
        listener = new MdmProductEventListener(projectionService, new ObjectMapper());
    }

    @Test
    @DisplayName("Variant 事件按聚合根字段 code/name/modelCode 映射投影")
    void should_map_variant_aggregate_payload() {
        String json = """
            {"id":11,"code":"VAR001","name":"豪华版","nameLocal":"Luxury","modelCode":"MOD001",
             "version":3,"status":"ACTIVE","rowValid":true}
            """;
        when(projectionService.getModelOptional("MOD001"))
            .thenReturn(Optional.of(MdmProjectionModelPo.builder().modelName("车型一").build()));

        listener.handleVariantChanged(record("mdm.variant", "VAR001", json), acknowledgment);

        ArgumentCaptor<MdmProjectionVariantPo> captor = ArgumentCaptor.forClass(MdmProjectionVariantPo.class);
        verify(projectionService).saveOrUpdateVariant(captor.capture());
        MdmProjectionVariantPo po = captor.getValue();
        assertEquals("VAR001", po.getVariantCode());
        assertEquals("豪华版", po.getVariantName());
        assertEquals("MOD001", po.getModelCode());
        assertEquals("车型一", po.getModelName());
        assertEquals("ACTIVE", po.getStatus());
        assertNull(po.getStandardOptions());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Configuration 事件按 code/variantCode 映射，缺失 optionCodes 时不填充")
    void should_map_configuration_aggregate_payload() {
        String json = """
            {"code":"CFG001","name":"配置一","variantCode":"VAR001","status":"INACTIVE","guidePrice":199900.00}
            """;

        listener.handleConfigurationChanged(record("mdm.configuration", "CFG001", json), acknowledgment);

        ArgumentCaptor<MdmProjectionConfigurationPo> captor =
            ArgumentCaptor.forClass(MdmProjectionConfigurationPo.class);
        verify(projectionService).saveOrUpdateConfiguration(captor.capture());
        MdmProjectionConfigurationPo po = captor.getValue();
        assertEquals("CFG001", po.getConfigurationCode());
        assertEquals("VAR001", po.getVariantCode());
        assertEquals(0, new BigDecimal("199900.00").compareTo(po.getGuidePrice()));
        assertEquals("INACTIVE", po.getStatus());
        assertNull(po.getOptionCodes());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("OptionCode 事件按 code/name/optionFamilyCode 映射")
    void should_map_option_code_aggregate_payload() {
        String json = """
            {"code":"OPT001","name":"全景天窗","optionFamilyCode":"FAM001","status":"ACTIVE"}
            """;

        listener.handleOptionChanged(record("mdm.option-code", "OPT001", json), acknowledgment);

        ArgumentCaptor<MdmProjectionOptionPo> captor = ArgumentCaptor.forClass(MdmProjectionOptionPo.class);
        verify(projectionService).saveOrUpdateOption(captor.capture());
        MdmProjectionOptionPo po = captor.getValue();
        assertEquals("OPT001", po.getOptionCode());
        assertEquals("全景天窗", po.getOptionName());
        assertEquals("FAM001", po.getOptionFamilyCode());
        assertNull(po.getMutexWith());
        assertNull(po.getBundleWith());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("OptionFamily 事件按 code/name 映射投影")
    void should_map_option_family_aggregate_payload() {
        String json = """
            {"code":"FAM001","name":"外观","category":"EXTERIOR","status":"ACTIVE"}
            """;

        listener.handleOptionFamilyChanged(record("mdm.option-family", "FAM001", json), acknowledgment);

        ArgumentCaptor<MdmProjectionOptionFamilyPo> captor =
            ArgumentCaptor.forClass(MdmProjectionOptionFamilyPo.class);
        verify(projectionService).saveOrUpdateOptionFamily(captor.capture());
        MdmProjectionOptionFamilyPo po = captor.getValue();
        assertEquals("FAM001", po.getOptionFamilyCode());
        assertEquals("外观", po.getOptionFamilyName());
        assertEquals("ACTIVE", po.getStatus());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Model 事件兼容 envelope payload 子节点与 carLineCode 字段")
    void should_map_model_envelope_payload() {
        String json = """
            {"eventType":"mdm.product.model.created","entityId":"MOD001","version":1,
             "payload":{"code":"MOD001","name":"车型一","carLineCode":"CAR001","status":"ACTIVE"}}
            """;

        listener.handleModelChanged(record("mdm.model", "MOD001", json), acknowledgment);

        ArgumentCaptor<MdmProjectionModelPo> captor = ArgumentCaptor.forClass(MdmProjectionModelPo.class);
        verify(projectionService).saveOrUpdateModel(captor.capture());
        MdmProjectionModelPo po = captor.getValue();
        assertEquals("MOD001", po.getModelCode());
        assertEquals("车型一", po.getModelName());
        assertEquals("CAR001", po.getCarlineCode());
        assertNull(po.getVariantCodes());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("CarLine 事件缺少 code 时回退到 Kafka key")
    void should_fallback_to_record_key_for_carline() {
        String json = """
            {"name":"车系一","status":"ACTIVE"}
            """;

        listener.handleCarlineChanged(record("mdm.car-line", "CAR001", json), acknowledgment);

        ArgumentCaptor<MdmProjectionCarlinePo> captor = ArgumentCaptor.forClass(MdmProjectionCarlinePo.class);
        verify(projectionService).saveOrUpdateCarline(captor.capture());
        MdmProjectionCarlinePo po = captor.getValue();
        assertEquals("CAR001", po.getCarlineCode());
        assertEquals("车系一", po.getCarlineName());
        assertNull(po.getModelCodes());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("非法消息缺少业务主键时不写投影且不提交 offset")
    void should_not_ack_when_payload_invalid() {
        listener.handleVariantChanged(record("mdm.variant", null, "not-json"), acknowledgment);

        verify(projectionService, never()).saveOrUpdateVariant(any());
        verify(acknowledgment, never()).acknowledge();
    }

    private ConsumerRecord<String, String> record(String topic, String key, String value) {
        assertNotNull(topic);
        return new ConsumerRecord<>(topic, 0, 1L, key, value);
    }
}
