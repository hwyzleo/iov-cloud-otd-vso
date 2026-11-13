package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.otd.vso.api.vo.SupplementaryPaymentVo;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryAppointmentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryStaffResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderTimelineResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PurchaseAgreementResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.RefundResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.DeliveryAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PaymentAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PurchaseAgreementAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.RefundAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleAssignmentAppService;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MPT 运营只读查询契约测试（US-011 / 设计 §5.4.2）
 * <p>
 * 覆盖：业务 orderId 路径参数、权限点、只读端点集合、时间线操作人字段、
 * 退款场景与手续费可核对性、未发生预约返回 null、列表分页与占位端点清理。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MPT 运营只读查询 - 路径参数/权限点/时间线/退款核对/交付预约")
class MptOrderQueryControllerTest {

    private static final String BASE = "/api/mpt/vso/v1";
    private static final String READ_PERMISSION = "completeVehicle:order:info:query";

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
    @DisplayName("订单详情按业务订单 ID 查询，不使用数据库主键")
    void 订单详情使用业务订单ID() throws Exception {
        when(orderAppService.getById("ORD1")).thenReturn(OrderDetailResult.builder()
                .orderId("ORD1")
                .orderNo("SO001")
                .build());

        mockMvc.perform(get(BASE + "/ORD1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value("ORD1"))
                .andExpect(jsonPath("$.data.orderNo").value("SO001"));

        verify(orderAppService).getById("ORD1");
    }

    @Test
    @DisplayName("订单详情输出车型快照、客户支付信息与完整金额快照")
    void 订单详情完整字段契约() throws Exception {
        OrderAmount amount = new OrderAmount("AMT1");
        amount.setVehiclePrice(new Money(new BigDecimal("200000.00")));
        amount.setOptionPrice(new Money(new BigDecimal("10000.00")));
        amount.setDealPriceTotal(new Money(new BigDecimal("210000.00")));
        amount.setDepositAmount(new Money(new BigDecimal("5000.00")));
        amount.setDownPaymentAmount(new Money(new BigDecimal("5000.00")));
        amount.setPaidTotal(new Money(new BigDecimal("5000.00")));
        when(orderAppService.getById("ORD1")).thenReturn(OrderDetailResult.builder()
                .orderId("ORD1")
                .orderNo("SO001")
                .brandCode("BRAND_HC")
                .brandName("测试品牌")
                .saleModel("HC")
                .saleModelName("寒川")
                .modelCode("HC-M1")
                .modelName("寒川车型")
                .variantCode("HC-V1")
                .variantName("寒川版")
                .configurationCode("CFG-HC-001")
                .optionCodes(Collections.singletonList("OPT1"))
                .optionBreakdown(Collections.singletonList(new VehicleInfo.OptionBreakdownItem(
                        "OPT1", "COLOR", "颜色", "珍珠白", new BigDecimal("10000.00"))))
                .customerType("personal")
                .paymentMethod("full_payment")
                .purchasePlan(1)
                .orderPersonType(1)
                .orderPersonName("张三")
                .orderPersonIdType(1)
                .orderPersonIdNum("ID-NUM-1")
                .payState(20)
                .orderAmount(amount)
                .build());

        mockMvc.perform(get(BASE + "/ORD1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.brandName").value("测试品牌"))
                .andExpect(jsonPath("$.data.saleModelName").value("寒川"))
                .andExpect(jsonPath("$.data.modelName").value("寒川车型"))
                .andExpect(jsonPath("$.data.variantName").value("寒川版"))
                .andExpect(jsonPath("$.data.configurationCode").value("CFG-HC-001"))
                .andExpect(jsonPath("$.data.optionBreakdown[0].optionName").value("珍珠白"))
                .andExpect(jsonPath("$.data.customerType").value("personal"))
                .andExpect(jsonPath("$.data.paymentMethod").value("full_payment"))
                .andExpect(jsonPath("$.data.purchasePlan").value(1))
                .andExpect(jsonPath("$.data.orderPersonName").value("张三"))
                .andExpect(jsonPath("$.data.orderPersonIdNum").value("ID-NUM-1"))
                .andExpect(jsonPath("$.data.payState").value(20))
                .andExpect(jsonPath("$.data.orderAmount.vehiclePrice.amount").value(200000.00))
                .andExpect(jsonPath("$.data.orderAmount.optionPrice.amount").value(10000.00))
                .andExpect(jsonPath("$.data.orderAmount.dealPriceTotal.amount").value(210000.00))
                .andExpect(jsonPath("$.data.orderAmount.paidTotal.amount").value(5000.00));
    }

    @Test
    @DisplayName("时间线返回操作人、角色与操作来源")
    void 时间线返回操作人字段() throws Exception {
        when(orderAppService.getTimelineByOrderId("ORD1")).thenReturn(Collections.singletonList(
                OrderTimelineResult.builder()
                        .eventType("ORDER_CREATED")
                        .eventName("订单创建")
                        .operatorId("ACC1")
                        .operatorRole("order_user")
                        .operateSource("capp")
                        .relatedDocNo("SO001")
                        .build()));

        mockMvc.perform(get(BASE + "/ORD1/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].operatorId").value("ACC1"))
                .andExpect(jsonPath("$.data[0].operatorRole").value("order_user"))
                .andExpect(jsonPath("$.data[0].operateSource").value("capp"))
                .andExpect(jsonPath("$.data[0].relatedDocNo").value("SO001"));
    }

    @Test
    @DisplayName("支付与补款查询返回支付阶段和补款任务")
    void 支付与补款查询() throws Exception {
        when(paymentAppService.listPaymentsByOrderId("ORD1")).thenReturn(Collections.singletonList(
                PaymentResult.builder()
                        .paymentNo("P001")
                        .orderNo("SO001")
                        .paymentStage("EARNEST_MONEY")
                        .paymentAmount(new BigDecimal("5000.00"))
                        .paymentStatus("PAID")
                        .build()));
        when(orderAppService.getSupplementaryPaymentsByOrderId("ORD1")).thenReturn(Collections.singletonList(
                SupplementaryPaymentVo.builder()
                        .supplementaryNo("SP001")
                        .orderNo("SO001")
                        .supplementaryAmount(new BigDecimal("15000.00"))
                        .supplementaryStatus("completed")
                        .build()));

        mockMvc.perform(get(BASE + "/ORD1/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].paymentStage").value("EARNEST_MONEY"))
                .andExpect(jsonPath("$.data[0].paymentStatus").value("PAID"));

        mockMvc.perform(get(BASE + "/ORD1/supplements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].supplementaryNo").value("SP001"))
                .andExpect(jsonPath("$.data[0].supplementaryStatus").value("completed"));
    }

    @Test
    @DisplayName("退款查询返回退款场景与手续费，可核对 max(已支付×5%,500) 规则")
    void 退款查询可核对手续费() throws Exception {
        // 锁单退款：已支付 20000，手续费 max(1000, 500) = 1000，可退 19000
        when(refundAppService.listByOrderId("ORD1")).thenReturn(Collections.singletonList(
                RefundResult.builder()
                        .refundNo("REF001")
                        .orderNo("SO001")
                        .refundScene("arrange_production")
                        .refundAmount(new BigDecimal("19000.00"))
                        .refundFee(new BigDecimal("1000.00"))
                        .refundStatus("pending")
                        .applyTime(LocalDateTime.of(2026, 11, 10, 10, 0))
                        .items(Collections.emptyList())
                        .build()));

        mockMvc.perform(get(BASE + "/ORD1/refunds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].refundScene").value("arrange_production"))
                .andExpect(jsonPath("$.data[0].refundAmount").value(19000.00))
                .andExpect(jsonPath("$.data[0].refundFee").value(1000.00))
                .andExpect(jsonPath("$.data[0].refundStatus").value("pending"));
    }

    @Test
    @DisplayName("协议确认快照与交付预约查询")
    void 协议快照与交付预约查询() throws Exception {
        when(purchaseAgreementAppService.listConfirmations("ORD1")).thenReturn(Collections.singletonList(
                PurchaseAgreementResult.builder()
                        .agreementCode("VEHICLE_PURCHASE")
                        .versionNo(1)
                        .title("购车协议")
                        .build()));
        when(deliveryAppService.getByOrderId("ORD1")).thenReturn(DeliveryAppointmentResult.builder()
                .appointmentNo("DA001")
                .orderNo("SO001")
                .appointmentStatus("booked")
                .build());

        mockMvc.perform(get(BASE + "/ORD1/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].agreementCode").value("VEHICLE_PURCHASE"))
                .andExpect(jsonPath("$.data[0].versionNo").value(1));

        mockMvc.perform(get(BASE + "/ORD1/delivery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appointmentNo").value("DA001"))
                .andExpect(jsonPath("$.data.appointmentStatus").value("booked"));
    }

    @Test
    @DisplayName("未发生预约返回 null，不合成预约单号")
    void 未发生预约返回空() throws Exception {
        when(deliveryAppService.getByOrderId("ORD1")).thenReturn(null);

        mockMvc.perform(get(BASE + "/ORD1/delivery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("只读端点统一使用 MPT 查询权限点")
    void 只读端点权限点() {
        List<String> readPaths = Arrays.asList("/{orderId}", "/{orderId}/timeline", "/{orderId}/payments",
                "/{orderId}/supplements", "/{orderId}/refunds", "/{orderId}/agreements", "/{orderId}/delivery");
        for (String path : readPaths) {
            Method method = findMethodByGetMapping(MptVsoController.class, path);
            assertNotNull(method, "只读端点应存在：" + path);
            RequiresPermissions permission = method.getAnnotation(RequiresPermissions.class);
            assertNotNull(permission, "只读端点必须声明权限点：" + path);
            assertTrue(Arrays.asList(permission.value()).contains(READ_PERMISSION),
                    "只读端点应使用 " + READ_PERMISSION + "：" + path);
        }
    }

    @Test
    @DisplayName("订单域路径参数为业务 orderId（String），不是主键 Long")
    void 路径参数为业务订单ID() throws Exception {
        Method getInfo = MptVsoController.class.getDeclaredMethod("getInfo", String.class);
        assertNotNull(getInfo, "详情端点应使用 String 业务订单 ID 参数");
        java.lang.reflect.Parameter parameter = getInfo.getParameters()[0];
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        assertNotNull(pathVariable, "详情端点必须使用 @PathVariable");
        assertEquals("orderId", pathVariable.value(), "路径变量名必须为业务语义 orderId");
        assertEquals(String.class, parameter.getType(), "路径参数类型必须为 String 业务订单 ID");

        for (Method method : MptVsoController.class.getDeclaredMethods()) {
            for (java.lang.reflect.Parameter p : method.getParameters()) {
                PathVariable pv = p.getAnnotation(PathVariable.class);
                if (pv != null) {
                    assertFalse(pv.value().toLowerCase().contains("vehicleSaleOrderId"),
                            "不得使用 vehicleSaleOrderId 之类与语义不符的路径变量：" + method.getName());
                    assertFalse(p.getType().equals(Long.class),
                            "路径参数不得使用 Long 主键类型：" + method.getName());
                    assertFalse("id".equals(pv.value()),
                            "路径参数不得使用数据库主键 id：" + method.getName());
                }
            }
        }
    }

    @Test
    @DisplayName("空实现占位端点已删除，列表端点为 PageResult + BaseRequest 查询对象")
    void 占位端点已删除() {
        List<String> getPaths = collectGetMappings(MptVsoController.class);

        assertFalse(getPaths.contains("/vehicleAssignment/list"), "不得保留无实现的配车记录列表占位端点");
        for (String path : getPaths) {
            assertFalse(path.isEmpty(), "不得保留根路径列表占位端点（与 /list 语义重复）");
        }
        assertTrue(getPaths.contains("/list"), "订单列表端点应保留");
        assertTrue(getPaths.contains("/vehicleAssignment/{orderNo}"), "配车信息查询端点应保留");

        // 列表端点必须接受 BaseRequest 派生查询对象且返回 PageResult（配合框架 startPage 数据库分页）
        for (String methodName : Arrays.asList("list", "listWithoutDeliveryPerson", "listTransport")) {
            Method method = findMethodByName(MptVsoController.class, methodName);
            assertNotNull(method, "列表端点应存在：" + methodName);
            assertEquals(1, method.getParameterCount(), methodName + " 应只接受一个查询对象参数");
            assertTrue(net.hwyz.iov.cloud.framework.common.bean.BaseRequest.class
                            .isAssignableFrom(method.getParameters()[0].getType()),
                    methodName + " 查询对象必须继承 BaseRequest，以支持 params[beginTime]/params[endTime]");
            assertTrue(method.getGenericReturnType().getTypeName().contains("PageResult"),
                    methodName + " 必须返回 PageResult（数据库分页）");
        }
    }

    @Test
    @DisplayName("MPT 列表返回交付预约字段，交付人员分配页无需逐条查询")
    void 列表返回预约字段() throws Exception {
        Field appointmentNo = VehicleSaleOrderMpt.class.getDeclaredField("appointmentNo");
        Field appointmentTime = VehicleSaleOrderMpt.class.getDeclaredField("appointmentTime");
        Field appointmentStatus = VehicleSaleOrderMpt.class.getDeclaredField("appointmentStatus");
        assertNotNull(appointmentNo);
        assertNotNull(appointmentTime);
        assertNotNull(appointmentStatus);
        assertEquals(String.class, appointmentNo.getType());
        assertEquals(LocalDateTime.class, appointmentTime.getType());
        assertEquals(String.class, appointmentStatus.getType());

        OrderListResult listResult = OrderListResult.builder()
                .orderId("ORD1")
                .orderNo("SO001")
                .build();
        listResult.setAppointmentNo("DA001");
        listResult.setAppointmentStatus("booked");
        assertNotNull(listResult.getAppointmentNo(), "订单列表结果必须承载预约单号");
    }

    @Test
    @DisplayName("时间线结果包含 operatorId，退款结果包含 refundScene")
    void 结果字段契约() throws Exception {
        assertNotNull(OrderTimelineResult.class.getDeclaredField("operatorId"),
                "时间线结果必须输出 operatorId");
        assertNotNull(RefundResult.class.getDeclaredField("refundScene"),
                "退款结果必须输出 refundScene 以便核对手续费规则");
        assertNotNull(RefundResult.class.getDeclaredField("refundFee"));
        assertNotNull(RefundResult.class.getDeclaredField("applyTime"));
        assertNotNull(RefundResult.class.getDeclaredField("refundTime"));
    }

    @Test
    @DisplayName("订单列表查询对象继承 BaseRequest，支持 params[beginTime]/params[endTime] 时间范围")
    void 列表支持时间范围() {
        assertTrue(net.hwyz.iov.cloud.framework.common.bean.BaseRequest.class
                        .isAssignableFrom(VehicleSaleOrderMpt.class),
                "VehicleSaleOrderMpt 必须继承 BaseRequest 才能绑定 params[beginTime]");
        assertTrue(net.hwyz.iov.cloud.framework.common.bean.BaseRequest.class
                        .isAssignableFrom(net.hwyz.iov.cloud.otd.vso.api.vo.mpt.TransportOrderMpt.class),
                "TransportOrderMpt 必须继承 BaseRequest 才能绑定 params[beginTime]");
        VehicleSaleOrderMpt vo = new VehicleSaleOrderMpt();
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("beginTime", "2026-11-01 00:00:00");
        vo.setParams(params);
        assertNotNull(vo.getParams().get("beginTime"));
    }

    @Test
    @DisplayName("交付人员分配列表门店名称按 storeCode 去重后获取（不逐行远程调用）")
    void 门店名称去重获取() throws Exception {
        when(orderAppService.search(any(OrderQuery.class))).thenReturn(Arrays.asList(
                OrderListResult.builder().orderId("ORD1").orderNo("SO001").deliveryStoreCode("D001").build(),
                OrderListResult.builder().orderId("ORD2").orderNo("SO002").deliveryStoreCode("D001").build(),
                OrderListResult.builder().orderId("ORD3").orderNo("SO003").deliveryStoreCode("D002").build()));
        when(orgDealershipService.getByCode("D001"))
                .thenReturn(net.hwyz.iov.cloud.edd.org.api.vo.DealershipExService.builder()
                        .code("D001").name("交付中心A").build());
        when(orgDealershipService.getByCode("D002"))
                .thenReturn(net.hwyz.iov.cloud.edd.org.api.vo.DealershipExService.builder()
                        .code("D002").name("交付中心B").build());

        mockMvc.perform(get(BASE + "/listWithoutDeliveryPerson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].deliveryStoreName").value("交付中心A"))
                .andExpect(jsonPath("$.data.items[2].deliveryStoreName").value("交付中心B"));

        verify(orgDealershipService, org.mockito.Mockito.times(1)).getByCode("D001");
        verify(orgDealershipService, org.mockito.Mockito.times(1)).getByCode("D002");
        verify(orgDealershipService, org.mockito.Mockito.times(2)).getByCode(any());
    }

    @Test
    @DisplayName("可改配列表状态与改配校验共用同一事实（210/300/310，不含 400）")
    void 可改配列表状态一致() throws Exception {
        when(orderAppService.search(any(OrderQuery.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE + "/listModelConfigChangeable"))
                .andExpect(status().isOk());

        ArgumentCaptor<OrderQuery> captor = ArgumentCaptor.forClass(OrderQuery.class);
        verify(orderAppService).search(captor.capture());
        List<Integer> range = captor.getValue().getOrderStateRange();
        assertNotNull(range);
        assertEquals(net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.configModifiableStateValues(), range,
                "列表筛选必须与改配校验使用同一状态集合");
        assertEquals(3, range.size());
        assertTrue(range.containsAll(Arrays.asList(210, 300, 310)), "应包含意向金已付/定金待付/定金已付");
        assertFalse(range.contains(400), "已锁单不能进可改配列表");
    }

    @Test
    @DisplayName("交付中心人员列表不再返回空实现，按远程分页透传")
    void 交付中心人员列表() throws Exception {
        when(orderAppService.searchDeliveryStaff("D001", 2, 10)).thenReturn(new PageResult<>(
                Collections.singletonList(DeliveryStaffResult.builder()
                        .dealershipCode("D001")
                        .dealershipName("交付中心A")
                        .userId(1001L)
                        .userName("staff01")
                        .nickName("张三")
                        .notDeliveryOrderCount(3)
                        .build()),
                21L, 2, 10));

        mockMvc.perform(get(BASE + "/listDeliveryCenterStaff")
                        .param("dealershipCode", "D001")
                        .param("pageNum", "2")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(21))
                .andExpect(jsonPath("$.data.items[0].userName").value("staff01"))
                .andExpect(jsonPath("$.data.items[0].notDeliveryOrderCount").value(3));

        verify(orderAppService).searchDeliveryStaff("D001", 2, 10);
    }

    @Test
    @DisplayName("MPT 订单 VO 字段必须由 Result DTO 承载（仅主键与加密敏感字段除外）")
    void 订单VO字段均有Result承载() {
        List<String> intentionallyEmpty = Arrays.asList("orderPersonPhone", "orderPersonId");
        List<String> missing = new ArrayList<>();
        for (Field voField : VehicleSaleOrderMpt.class.getDeclaredFields()) {
            String name = voField.getName();
            if (intentionallyEmpty.contains(name) || java.lang.reflect.Modifier.isStatic(voField.getModifiers())) {
                continue;
            }
            if (!hasField(OrderListResult.class, name) && !hasField(OrderDetailResult.class, name)) {
                missing.add(name);
            }
        }
        assertTrue(missing.isEmpty(), "以下 MPT 字段没有 Result DTO 承载，前端将恒为空：" + missing);
    }

    @Test
    @DisplayName("MPT VO 不含数据库主键，加密隐私字段不得被映射")
    void 主键与隐私字段不映射() throws Exception {
        for (Class<?> voType : Arrays.asList(VehicleSaleOrderMpt.class,
                net.hwyz.iov.cloud.otd.vso.api.vo.mpt.TransportOrderMpt.class,
                net.hwyz.iov.cloud.otd.vso.api.vo.mpt.DeliveryCenterStaffMpt.class)) {
            assertFalse(hasField(voType, "id"),
                    voType.getSimpleName() + " 不得保留恒为 null 的数据库主键字段，前端统一使用 orderId");
        }

        for (Class<?> resultType : Arrays.asList(OrderListResult.class, OrderDetailResult.class)) {
            assertFalse(hasField(resultType, "id"),
                    resultType.getSimpleName() + " 不得承载数据库主键，避免被前端当作路由参数");
            assertFalse(hasField(resultType, "orderPersonPhone"),
                    resultType.getSimpleName() + " 不得承载加密手机号（解密服务属 O-009）");
            assertFalse(hasField(resultType, "orderPersonId"),
                    resultType.getSimpleName() + " 不得承载加密证件号（解密服务属 O-009）");
        }
    }

    private static boolean hasField(Class<?> type, String name) {
        try {
            type.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    // ==================== helpers ====================

    private static List<String> collectGetMappings(Class<?> controller) {
        List<String> result = new ArrayList<>();
        for (Method method : controller.getDeclaredMethods()) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
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

    private static Method findMethodByGetMapping(Class<?> controller, String path) {
        for (Method method : controller.getDeclaredMethods()) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            if (mapping == null) {
                continue;
            }
            String[] values = mapping.value().length > 0 ? mapping.value() : mapping.path();
            if (Arrays.asList(values).contains(path)) {
                return method;
            }
        }
        return null;
    }

    private static Method findMethodByName(Class<?> controller, String name) {
        for (Method method : controller.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
