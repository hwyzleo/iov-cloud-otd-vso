package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PurchaseAgreementResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PurchaseAgreementAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MPT 购车协议后台管理契约测试
 * <p>
 * 覆盖：权限点（list/edit/remove）、列表分页（PageResult.total 来自分页插件）、路由保持。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MPT 购车协议管理 - 权限点/分页/路由")
class MptPurchaseAgreementControllerTest {

    private static final String BASE = "/api/mpt/vso/v1/purchaseAgreement";

    @Mock
    private PurchaseAgreementAppService purchaseAgreementAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MptPurchaseAgreementController controller = new MptPurchaseAgreementController(purchaseAgreementAppService);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("协议列表返回 PageResult，total 来自分页插件而非当前页条数")
    void 协议列表分页() throws Exception {
        Page<PurchaseAgreementResult> page = new Page<>(1, 20, true);
        page.setTotal(37L);
        page.add(PurchaseAgreementResult.builder()
                .agreementCode("VEHICLE_PURCHASE")
                .versionNo(1)
                .title("购车协议")
                .build());
        when(purchaseAgreementAppService.search(null, null, null)).thenReturn(page);

        mockMvc.perform(get(BASE + "/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(37))
                .andExpect(jsonPath("$.data.items[0].agreementCode").value("VEHICLE_PURCHASE"));

        verify(purchaseAgreementAppService).search(null, null, null);
    }

    @Test
    @DisplayName("协议后台端点必须声明权限点（list/edit/remove）")
    void 协议端点权限点() {
        assertPermission("list", GetMapping.class, "completeVehicle:order:agreement:list");
        assertPermission("save", PostMapping.class, "completeVehicle:order:agreement:edit");
        assertPermission("offShelf", PostMapping.class, "completeVehicle:order:agreement:remove");
    }

    @Test
    @DisplayName("协议后台端点不得出现无权限点的方法")
    void 协议端点不得裸奔() {
        List<String> missing = new ArrayList<>();
        for (Method method : MptPurchaseAgreementController.class.getDeclaredMethods()) {
            boolean mapped = method.getAnnotation(GetMapping.class) != null
                    || method.getAnnotation(PostMapping.class) != null;
            if (!mapped) {
                continue;
            }
            assertNotNull(method.getName());
            if (method.getAnnotation(RequiresPermissions.class) == null) {
                missing.add(method.getName());
            }
        }
        assertTrue(missing.isEmpty(), "以下协议后台端点缺少 @RequiresPermissions：" + missing);
    }

    @Test
    @DisplayName("原有路由契约保持不变")
    void 路由保持不变() {
        List<String> getPaths = new ArrayList<>();
        List<String> postPaths = new ArrayList<>();
        for (Method method : MptPurchaseAgreementController.class.getDeclaredMethods()) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping != null) {
                getPaths.addAll(Arrays.asList(getMapping.value().length > 0 ? getMapping.value() : getMapping.path()));
            }
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (postMapping != null) {
                postPaths.addAll(Arrays.asList(postMapping.value().length > 0 ? postMapping.value() : postMapping.path()));
            }
        }
        assertTrue(getPaths.contains("/list"));
        assertTrue(postPaths.contains("/action/save"));
        assertTrue(postPaths.contains("/action/offShelf"));
        assertFalse(getPaths.isEmpty() || postPaths.isEmpty());
    }

    private void assertPermission(String methodName, Class<? extends java.lang.annotation.Annotation> mapping,
                                 String expected) {
        for (Method method : MptPurchaseAgreementController.class.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            assertNotNull(method.getAnnotation(mapping), methodName + " 缺少映射注解");
            RequiresPermissions permission = method.getAnnotation(RequiresPermissions.class);
            assertNotNull(permission, methodName + " 缺少 @RequiresPermissions");
            assertTrue(Arrays.asList(permission.value()).contains(expected),
                    methodName + " 权限点应为 " + expected);
            return;
        }
        throw new AssertionError("未找到方法：" + methodName);
    }
}
