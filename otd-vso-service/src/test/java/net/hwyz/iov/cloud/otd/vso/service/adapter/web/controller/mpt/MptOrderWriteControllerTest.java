package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import net.hwyz.iov.cloud.framework.common.bean.BaseRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CancelOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.LockOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigByOperatorCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ReassignVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.service.DeliveryAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PaymentAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PurchaseAgreementAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.RefundAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleAssignmentAppService;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MPT 订单写动作契约测试（设计 §5.4.1 / §5.4.2 约束 10）
 * <p>
 * 覆盖：操作人来自安全上下文（不得用 X-Operator-Id 请求头）、写动作路由集合、
 * 业务异常不再被 catch 吞成“操作失败”。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MPT 订单写动作 - 操作人来源/路由/异常不吞")
class MptOrderWriteControllerTest {

    private static final String BASE = "/api/mpt/vso/v1";

    @Mock
    private OrderAppService orderAppService;
    @Mock
    private OrgDealershipService orgDealershipService;
    @Mock
    private VehicleAssignmentAppService vehicleAssignmentAppService;
    @Mock
    private PaymentAppService paymentAppService;
    @Mock
    private RefundAppService refundAppService;
    @Mock
    private DeliveryAppService deliveryAppService;
    @Mock
    private PurchaseAgreementAppService purchaseAgreementAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MptVsoController controller = new MptVsoController(orderAppService, orgDealershipService,
                vehicleAssignmentAppService, paymentAppService, refundAppService, deliveryAppService,
                purchaseAgreementAppService);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("写动作不使用 X-Operator-Id 请求头充当操作人")
    void 不使用_操作人请求头() {
        for (Method method : MptVsoController.class.getDeclaredMethods()) {
            for (java.lang.reflect.Parameter parameter : method.getParameters()) {
                RequestHeader header = parameter.getAnnotation(RequestHeader.class);
                if (header == null) {
                    continue;
                }
                assertFalse(Arrays.asList(header.value()).contains("X-Operator-Id"),
                        "不得以 X-Operator-Id 请求头充当操作人：" + method.getName());
                assertFalse(Arrays.asList(header.name()).contains("X-Operator-Id"),
                        "不得以 X-Operator-Id 请求头充当操作人：" + method.getName());
            }
        }
    }

    @Test
    @DisplayName("写动作路由集合保持既有契约")
    void 写动作路由存在() {
        List<String> postPaths = collectPostMappings(MptVsoController.class);
        for (String path : Arrays.asList("/action/assignDeliveryPerson", "/action/assignVehicle",
                "/action/applyTransport", "/action/reassignVehicle", "/action/unbindVehicle",
                "/{orderId}/audit/pass", "/{orderId}/audit/reject", "/{orderId}/audit/resubmit",
                "/{orderId}/lock", "/{orderId}/close", "/{orderId}/modifyConfig")) {
            assertTrue(postPaths.contains(path), "写动作路由应存在：" + path);
        }
        assertFalse(postPaths.stream().anyMatch(p -> p.endsWith("/action/pay")),
                "MPT 不得提供直接支付路由");
    }

    @Test
    @DisplayName("配车动作按业务订单号委托应用服务")
    void 配车委托() throws Exception {
        mockMvc.perform(post(BASE + "/action/assignVehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"SO001\",\"vin\":\"VIN001\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<AssignVehicleCmd> captor = ArgumentCaptor.forClass(AssignVehicleCmd.class);
        verify(orderAppService).assignVehicle(captor.capture());
        assertEquals("SO001", captor.getValue().getOrderNo());
        assertEquals("VIN001", captor.getValue().getVin());
    }

    @Test
    @DisplayName("换绑 VIN 委托配车应用服务且操作人来自安全上下文")
    void 换绑委托() throws Exception {
        mockMvc.perform(post(BASE + "/action/reassignVehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"SO001\",\"newVin\":\"VIN002\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<ReassignVehicleCmd> captor = ArgumentCaptor.forClass(ReassignVehicleCmd.class);
        verify(vehicleAssignmentAppService).reassignVehicle(captor.capture());
        assertEquals("SO001", captor.getValue().getOrderNo());
        assertEquals("VIN002", captor.getValue().getNewVin());
    }

    @Test
    @DisplayName("锁单按业务订单 ID 委托，操作人来自安全上下文而非请求头")
    void 锁单委托() throws Exception {
        mockMvc.perform(post(BASE + "/ORD1/lock"))
                .andExpect(status().isOk());

        ArgumentCaptor<LockOrderCmd> captor = ArgumentCaptor.forClass(LockOrderCmd.class);
        verify(orderAppService).lockOrder(captor.capture());
        assertEquals("ORD1", captor.getValue().getOrderId());
    }

    @Test
    @DisplayName("关闭订单必须提供原因")
    void 关闭订单必须提供原因() throws Exception {
        mockMvc.perform(post(BASE + "/ORD1/close"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(BASE + "/ORD1/close").param("reason", "客户取消"))
                .andExpect(status().isOk());

        ArgumentCaptor<CancelOrderCmd> captor = ArgumentCaptor.forClass(CancelOrderCmd.class);
        verify(orderAppService).cancelOrder(captor.capture());
        assertEquals("ORD1", captor.getValue().getOrderId());
        assertEquals("客户取消", captor.getValue().getReason());
        assertEquals("CLOSE", captor.getValue().getOperateType());
    }

    @Test
    @DisplayName("业务异常不被 catch 吞成“操作失败”，交由统一异常处理返回错误码")
    void 业务异常不吞() {
        doThrow(new OrderStateNotAllowedException("订单状态不允许锁单"))
                .when(orderAppService).lockOrder(any());

        Exception ex = assertThrows(Exception.class,
                () -> mockMvc.perform(post(BASE + "/ORD1/lock")).andReturn());
        Throwable cause = ex;
        boolean found = false;
        while (cause != null) {
            if (cause instanceof OrderStateNotAllowedException) {
                found = true;
                break;
            }
            cause = cause.getCause();
        }
        assertTrue(found, "业务异常必须向上抛出，不得被 Controller 转成“操作失败”");
    }

    @Test
    @DisplayName("写动作请求对象不得退化为 BaseRequest 裸 Map 传参")
    void 写动作使用具名请求对象() {
        for (Method method : MptVsoController.class.getDeclaredMethods()) {
            if (method.getAnnotation(PostMapping.class) == null) {
                continue;
            }
            for (Class<?> type : method.getParameterTypes()) {
                assertFalse(BaseRequest.class.equals(type),
                        "写动作必须使用具名请求对象：" + method.getName());
                assertNotNull(type);
            }
        }
    }

    @Test
    @DisplayName("逻辑删除端点已移除，只保留物理删除")
    void 逻辑删除端点已移除() {
        List<String> deletePaths = new ArrayList<>();
        for (Method method : MptVsoController.class.getDeclaredMethods()) {
            org.springframework.web.bind.annotation.DeleteMapping mapping =
                    method.getAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class);
            if (mapping == null) {
                continue;
            }
            String[] values = mapping.value().length > 0 ? mapping.value() : mapping.path();
            deletePaths.addAll(Arrays.asList(values));
        }
        assertTrue(deletePaths.contains("/physical/{orderId}"), "物理删除端点应保留");
        assertFalse(deletePaths.contains("/{orderNo}"), "逻辑删除端点应移除，避免被误用为可恢复删除");
        assertEquals(1, deletePaths.size(), "只应保留物理删除一个 DELETE 端点");
    }

    @Test
    @DisplayName("全部写动作必须声明权限点（不得裸奔）")
    void 写动作必须有权限点() {
        List<String> missing = new ArrayList<>();
        for (Method method : MptVsoController.class.getDeclaredMethods()) {
            if (method.getAnnotation(PostMapping.class) == null) {
                continue;
            }
            if (method.getAnnotation(net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions.class) == null) {
                missing.add(method.getName());
            }
        }
        assertTrue(missing.isEmpty(), "以下写动作缺少 @RequiresPermissions：" + missing);
    }

    @Test
    @DisplayName("MPT 改配提交接口存在且使用改配权限点")
    void 改配提交接口与权限() {
        for (Method method : MptVsoController.class.getDeclaredMethods()) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            if (mapping == null) {
                continue;
            }
            String[] values = mapping.value().length > 0 ? mapping.value() : mapping.path();
            if (!Arrays.asList(values).contains("/{orderId}/modifyConfig")) {
                continue;
            }
            net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions permission =
                    method.getAnnotation(net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions.class);
            assertNotNull(permission, "改配提交必须声明权限点");
            assertTrue(Arrays.asList(permission.value()).contains("completeVehicle:order:changeModel:edit"),
                    "改配提交应使用 completeVehicle:order:changeModel:edit");
            return;
        }
        throw new AssertionError("缺少 /{orderId}/modifyConfig 改配提交端点");
    }

    @Test
    @DisplayName("改配提交按业务订单 ID 委托，操作人来自安全上下文")
    void 改配提交委托() throws Exception {
        mockMvc.perform(post(BASE + "/ORD1/modifyConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantCode\":\"V002\",\"optionCodes\":[\"OPT1\",\"OPT2\"],\"reason\":\"客户要求\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<ModifyOrderConfigByOperatorCmd> captor =
                ArgumentCaptor.forClass(ModifyOrderConfigByOperatorCmd.class);
        verify(orderAppService).modifyConfigByOperator(captor.capture());
        assertEquals("ORD1", captor.getValue().getOrderId());
        assertEquals("V002", captor.getValue().getVariantCode());
        assertEquals(Arrays.asList("OPT1", "OPT2"), captor.getValue().getOptionCodes());
        assertEquals("客户要求", captor.getValue().getReason());
    }

    @Test
    @DisplayName("改配提交必须提供 optionCodes")
    void 改配提交必须提供选项() throws Exception {
        mockMvc.perform(post(BASE + "/ORD1/modifyConfig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantCode\":\"V002\",\"optionCodes\":[]}"))
                .andExpect(status().isBadRequest());
    }

    private static List<String> collectPostMappings(Class<?> controller) {
        List<String> result = new ArrayList<>();
        for (Method method : controller.getDeclaredMethods()) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            if (mapping == null) {
                continue;
            }
            String[] values = mapping.value().length > 0 ? mapping.value() : mapping.path();
            if (values.length == 0) {
                result.add("");
            } else {
                result.addAll(Arrays.asList(values));
            }
        }
        return result;
    }
}
