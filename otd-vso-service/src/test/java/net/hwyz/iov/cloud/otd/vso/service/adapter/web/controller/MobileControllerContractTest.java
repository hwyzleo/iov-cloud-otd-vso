package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller;

import net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile.MobileOrderDealershipController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile.MobileSaleModelController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile.MobileVsoController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt.MptPurchaseAgreementController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.open.OpenVsoCallbackController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.EarnestMoneyOrderRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request.SavePurchaseAgreementRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * C 端接口契约测试（§14：移除直接支付路由、协议与退款/时间线接口存在、必填校验）
 *
 * @author VSO Team
 */
@DisplayName("C 端接口契约 - 直接支付路由移除/协议与退款接口")
class MobileControllerContractTest {

    @Test
    @DisplayName("公开直接修改支付状态的 /order/action/pay 路由已移除")
    void 直接支付路由已移除() {
        List<String> mappings = collectMappings(MobileVsoController.class, PostMapping.class);
        assertFalse(mappings.contains("/order/action/pay"),
                "不得存在公开的 /order/action/pay 直接支付接口");
        assertFalse(mappings.stream().anyMatch(path -> path.endsWith("/action/pay")),
                "不得存在任何直接支付路由");
    }

    @Test
    @DisplayName("退款、时间线、协议查询与门店选项接口存在")
    void 必要路由存在() {
        List<String> mobilePost = collectMappings(MobileVsoController.class, PostMapping.class);
        List<String> mobileGet = collectMappings(MobileVsoController.class, GetMapping.class);
        assertTrue(mobilePost.contains("/order/action/requestRefund"), "退款申请路由应存在");
        assertTrue(mobilePost.contains("/order/action/reopenForPayment"),
                "支付超时恢复路由应存在（CR-005）");
        assertTrue(mobileGet.contains("/order/{orderNo}/timeline"), "订单时间线路由应存在");

        List<String> saleModelGet = collectMappings(MobileSaleModelController.class, GetMapping.class);
        assertTrue(saleModelGet.contains("/purchaseAgreements/{saleModelCode}"), "协议查询路由应存在");

        List<String> mptPost = collectMappings(MptPurchaseAgreementController.class, PostMapping.class);
        List<String> mptGet = collectMappings(MptPurchaseAgreementController.class, GetMapping.class);
        assertTrue(mptPost.contains("/action/save"), "后台保存协议路由应存在");
        assertTrue(mptPost.contains("/action/offShelf"), "后台下架协议路由应存在");
        assertTrue(mptGet.contains("/list"), "后台协议列表路由应存在");

        List<String> dealershipGet = collectMappings(MobileOrderDealershipController.class, GetMapping.class);
        assertTrue(dealershipGet.contains("/dealership/list"), "订单详情门店选项查询路由应存在（CR-008）");
    }

    @Test
    @DisplayName("退款回调路由存在")
    void 退款回调路由存在() {
        List<String> callbackPost = collectMappings(OpenVsoCallbackController.class, PostMapping.class);
        assertTrue(callbackPost.contains("/payment"), "支付回调路由应存在");
        assertTrue(callbackPost.contains("/refund"), "退款回调路由应存在");
    }

    @Test
    @DisplayName("下单请求必须提交协议确认与选项数组")
    void 下单请求校验契约() throws Exception {
        Field confirmations = EarnestMoneyOrderRequestVo.class.getDeclaredField("agreementConfirmations");
        assertNotNull(confirmations.getAnnotation(jakarta.validation.constraints.NotNull.class),
                "agreementConfirmations 必须校验非空");

        Field optionCodes = EarnestMoneyOrderRequestVo.class.getDeclaredField("optionCodes");
        assertNotNull(optionCodes.getAnnotation(jakarta.validation.constraints.NotNull.class),
                "optionCodes 必须校验非空（允许空数组）");

        Field versionNo = SavePurchaseAgreementRequest.class.getDeclaredField("versionNo");
        assertNotNull(versionNo.getAnnotation(jakarta.validation.constraints.Min.class),
                "协议版本号必须校验 >= 1");
    }

    private <A extends java.lang.annotation.Annotation> List<String> collectMappings(
            Class<?> controller, Class<A> annotationType) {
        List<String> result = new ArrayList<>();
        String base = "";
        RequestMapping classMapping = controller.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            base = classMapping.value()[0];
        }
        for (Method method : controller.getDeclaredMethods()) {
            A mapping = method.getAnnotation(annotationType);
            if (mapping == null) {
                continue;
            }
            String[] values;
            if (mapping instanceof PostMapping post) {
                values = post.value().length > 0 ? post.value() : post.path();
            } else {
                GetMapping get = (GetMapping) mapping;
                values = get.value().length > 0 ? get.value() : get.path();
            }
            if (values.length == 0) {
                result.add(base);
                continue;
            }
            for (String value : values) {
                result.add(value);
            }
        }
        return result;
    }
}
