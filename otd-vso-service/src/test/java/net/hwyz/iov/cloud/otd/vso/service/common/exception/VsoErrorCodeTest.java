package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VsoErrorCode 单元测试
 *
 * @author hwyz_leo
 */
class VsoErrorCodeTest {

    @Test
    void should_have_unique_error_codes() {
        Set<String> codes = new HashSet<>();
        for (VsoErrorCode errorCode : VsoErrorCode.values()) {
            assertTrue(codes.add(errorCode.getCode()), "错误码重复: " + errorCode.getCode());
        }
    }

    @Test
    void should_have_valid_error_code_format() {
        for (VsoErrorCode errorCode : VsoErrorCode.values()) {
            assertEquals(6, errorCode.getCode().length(), "错误码长度应为6位: " + errorCode.getCode());
            assertTrue(errorCode.getCode().startsWith("201"), "错误码应以201开头: " + errorCode.getCode());
        }
    }

    @Test
    void should_have_non_empty_messages() {
        for (VsoErrorCode errorCode : VsoErrorCode.values()) {
            assertNotNull(errorCode.getMessage(), "错误消息不能为空: " + errorCode.getCode());
            assertFalse(errorCode.getMessage().isEmpty(), "错误消息不能为空字符串: " + errorCode.getCode());
        }
    }

    @Test
    void should_have_correct_error_code_count() {
        assertEquals(35, VsoErrorCode.values().length, "错误码数量应为35个");
    }
}
