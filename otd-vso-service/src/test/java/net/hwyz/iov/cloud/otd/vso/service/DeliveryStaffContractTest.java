package net.hwyz.iov.cloud.otd.vso.service;

import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.DeliveryCenterStaffMpt;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryStaffResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OTD-VSO-DSN-CR-009 / INV-018、INV-020：交付人员契约门禁测试。
 * <p>
 * 交付人员的人员标识与展示字段必须来自 ORG 员工主数据（`employeeId/employeeCode/employeeName/phone`），
 * 不得再出现 MPT 账号字段（`userId/userName/nickName/phonenumber`）；VO 与 Result DTO 字段名保持一一对应，
 * 避免前端字段恒为空。
 *
 * @author VSO Team
 * @since 2026-11-17
 */
class DeliveryStaffContractTest {

    private static final List<String> EMPLOYEE_FIELDS =
            List.of("employeeId", "employeeCode", "employeeName", "phone");
    private static final List<String> FORBIDDEN_MPT_FIELDS =
            List.of("userId", "userName", "nickName", "phonenumber");

    @Test
    @DisplayName("DeliveryStaffResult 只含员工事实，不含 MPT 账号字段")
    void resultOnlyExposesEmployeeFacts() {
        List<String> fields = declaredFieldNames(DeliveryStaffResult.class);

        assertTrue(fields.containsAll(EMPLOYEE_FIELDS), "缺少员工字段：" + fields);
        for (String forbidden : FORBIDDEN_MPT_FIELDS) {
            assertFalse(fields.contains(forbidden), "不得保留 MPT 账号字段：" + forbidden);
        }
        assertTrue(fields.contains("notDeliveryOrderCount"), "未交付订单数必须保留");
    }

    @Test
    @DisplayName("DeliveryCenterStaffMpt 只含员工事实，不含 MPT 账号字段")
    void mptVoOnlyExposesEmployeeFacts() {
        List<String> fields = declaredFieldNames(DeliveryCenterStaffMpt.class);

        assertTrue(fields.containsAll(EMPLOYEE_FIELDS), "缺少员工字段：" + fields);
        for (String forbidden : FORBIDDEN_MPT_FIELDS) {
            assertFalse(fields.contains(forbidden), "不得保留 MPT 账号字段：" + forbidden);
        }
    }

    @Test
    @DisplayName("VO 字段必须由 Result DTO 同名字段承载，避免前端恒空")
    void voFieldsCarriedByResult() {
        List<String> resultFields = declaredFieldNames(DeliveryStaffResult.class);
        List<String> missing = declaredFieldNames(DeliveryCenterStaffMpt.class).stream()
                .filter(field -> !resultFields.contains(field))
                .collect(Collectors.toList());

        assertTrue(missing.isEmpty(), "以下 MPT VO 字段没有 Result DTO 承载：" + missing);
    }

    private List<String> declaredFieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isSynthetic() && !Modifier.isStatic(field.getModifiers()))
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}
