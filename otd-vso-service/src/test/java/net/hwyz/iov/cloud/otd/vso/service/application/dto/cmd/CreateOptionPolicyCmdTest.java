package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateOptionPolicyCmd - JSON 反序列化测试")
class CreateOptionPolicyCmdTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // 模拟 WebAutoConfiguration 的配置
        objectMapper.coercionConfigFor(LogicalType.Collection)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    }

    @Test
    @DisplayName("空字符串应反序列化为空列表")
    void should_deserialize_empty_string_to_empty_list() throws Exception {
        String json = """
            {
                "saleModelCode": "TEST001",
                "optionCode": "OPT001",
                "saleStatus": "active",
                "availableRegions": "",
                "channels": "",
                "bundleWith": "",
                "mutexWith": ""
            }
            """;

        CreateOptionPolicyCmd cmd = objectMapper.readValue(json, CreateOptionPolicyCmd.class);

        assertNotNull(cmd.getAvailableRegions());
        assertTrue(cmd.getAvailableRegions().isEmpty());
        assertNotNull(cmd.getChannels());
        assertTrue(cmd.getChannels().isEmpty());
        assertNotNull(cmd.getBundleWith());
        assertTrue(cmd.getBundleWith().isEmpty());
        assertNotNull(cmd.getMutexWith());
        assertTrue(cmd.getMutexWith().isEmpty());
    }

    @Test
    @DisplayName("正常数组应正确反序列化")
    void should_deserialize_array_correctly() throws Exception {
        String json = """
            {
                "saleModelCode": "TEST001",
                "optionCode": "OPT001",
                "saleStatus": "active",
                "availableRegions": ["CN", "US"],
                "channels": ["online"]
            }
            """;

        CreateOptionPolicyCmd cmd = objectMapper.readValue(json, CreateOptionPolicyCmd.class);

        assertEquals(2, cmd.getAvailableRegions().size());
        assertEquals("CN", cmd.getAvailableRegions().get(0));
        assertEquals(1, cmd.getChannels().size());
    }

    @Test
    @DisplayName("null 值应保持为 null")
    void should_keep_null_as_null() throws Exception {
        String json = """
            {
                "saleModelCode": "TEST001",
                "optionCode": "OPT001",
                "saleStatus": "active",
                "availableRegions": null
            }
            """;

        CreateOptionPolicyCmd cmd = objectMapper.readValue(json, CreateOptionPolicyCmd.class);

        assertNull(cmd.getAvailableRegions());
    }

    @Test
    @DisplayName("空数组应正确反序列化")
    void should_deserialize_empty_array_correctly() throws Exception {
        String json = """
            {
                "saleModelCode": "TEST001",
                "optionCode": "OPT001",
                "saleStatus": "active",
                "availableRegions": []
            }
            """;

        CreateOptionPolicyCmd cmd = objectMapper.readValue(json, CreateOptionPolicyCmd.class);

        assertNotNull(cmd.getAvailableRegions());
        assertTrue(cmd.getAvailableRegions().isEmpty());
    }
}
