package net.hwyz.iov.cloud.otd.vso.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.framework.web.autoconfigure.IovJacksonAutoConfiguration;
import net.hwyz.iov.cloud.otd.vso.api.vo.SupplementaryPaymentVo;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.VehicleAssignmentVo;
import net.hwyz.iov.cloud.otd.vso.api.vo.response.OrderDetailResponse;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request.PaymentCallbackRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.TimeoutTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OTD-VSO-DSN-CR-001 时间契约测试（兼容阶段：epoch-millis 写出 + ISO/旧整数双读）。
 * <p>
 * 加载真实 Boot JacksonAutoConfiguration + 框架 IovJacksonAutoConfiguration（FW-WEB-DSN-CR-001 P1），
 * 注入生产 mapper 断言 VSO 业务对象的 wire 输出与兼容读行为。对应 CR §4.1 测试与验收：
 * <ul>
 *   <li>epoch-millis 旧输出 golden 等值（Instant 绝对毫秒 / LocalDateTime 按 Asia/Shanghai 墙上时间毫秒）；</li>
 *   <li>ISO 字符串、10 位秒、13 位毫秒双读归一至同一时刻；null 保持可空性；非法格式 fail-closed；</li>
 *   <li>VSO 真实 VO/DTO 序列化 wire 字段断言（Instant 链路 / LocalDateTime 链路）；</li>
 *   <li>排除项不变：支付回调（PaymentCallbackRequest 入站双读）、Redis 持久载荷（TimeoutTask 往返）；</li>
 *   <li>启动探针：缺失 zone-id 阻断启动（配置门禁）。</li>
 * </ul>
 *
 * @author VSO Team
 */
@DisplayName("OTD-VSO-DSN-CR-001 - JSON java.time 时间契约测试")
class JacksonTimeContractTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Instant SAMPLE_INSTANT = Instant.parse("2026-10-28T12:43:32Z");
    private static final OffsetDateTime SAMPLE_OFFSET = OffsetDateTime.parse("2026-10-28T20:43:32+08:00");
    private static final LocalDateTime SAMPLE_LOCAL = LocalDateTime.parse("2026-10-14T14:26:13");

    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JacksonAutoConfiguration.class,
                        IovJacksonAutoConfiguration.class));
    }

    // ---------- 配置与启动探针 ----------

    @Test
    @DisplayName("配置 date-format=epoch-millis + zone-id=Asia/Shanghai 后启动探针通过")
    void 配置齐全_启动探针通过() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    assertNull(context.getStartupFailure(), "启动探针不应阻断，实际失败: " + context.getStartupFailure());
                    assertNotNull(context.getBean(ObjectMapper.class));
                });
    }

    @Test
    @DisplayName("缺失 zone-id 时启动探针阻断（存量时区未核验即禁止上线）")
    void 缺失zoneId_启动探针阻断() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("启动探针失败"));
                    assertTrue(context.getStartupFailure().getMessage().contains("zone-id"));
                });
    }

    @Test
    @DisplayName("非法 date-format 配置启动失败")
    void 非法dateFormat_启动失败() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=banana", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> assertNotNull(context.getStartupFailure()));
    }

    // ---------- epoch-millis 写出 golden ----------

    @Test
    @DisplayName("epoch-millis 模式：Instant/OffsetDateTime 输出 13 位毫秒，LocalDateTime 按 Asia/Shanghai 输出毫秒")
    void epochMillis模式_时间类型输出13位毫秒() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    JsonNode node = mapper.valueToTree(new TimeSample(SAMPLE_INSTANT, SAMPLE_OFFSET, SAMPLE_LOCAL));
                    assertTrue(node.get("instant").isIntegralNumber());
                    assertEquals(SAMPLE_INSTANT.toEpochMilli(), node.get("instant").longValue());
                    assertEquals(SAMPLE_OFFSET.toInstant().toEpochMilli(), node.get("offset").longValue());
                    assertEquals(SAMPLE_LOCAL.atZone(SHANGHAI).toInstant().toEpochMilli(), node.get("local").longValue());
                });
    }

    // ---------- 双读兼容（ISO + 旧整数） ----------

    @Test
    @DisplayName("双读：ISO 字符串 / 10 位秒 / 13 位毫秒归一至同一 Instant")
    void 双读_ISO_10位秒_13位毫秒同一时刻() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    long millis = SAMPLE_INSTANT.toEpochMilli();
                    long seconds = SAMPLE_INSTANT.getEpochSecond();
                    assertEquals(SAMPLE_INSTANT, mapper.readValue(String.valueOf(millis), Instant.class));
                    assertEquals(SAMPLE_INSTANT, mapper.readValue(String.valueOf(seconds), Instant.class));
                    assertEquals(SAMPLE_INSTANT, mapper.readValue("\"" + SAMPLE_INSTANT + "\"", Instant.class));
                    assertEquals(SAMPLE_INSTANT, mapper.readValue("\"" + SAMPLE_OFFSET + "\"", Instant.class));
                });
    }

    @Test
    @DisplayName("双读：LocalDateTime 数字按 Asia/Shanghai 还原，ISO 无 offset 墙上时间还原")
    void 双读_LocalDateTime_数字按上海时区还原() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    long millis = SAMPLE_LOCAL.atZone(SHANGHAI).toInstant().toEpochMilli();
                    assertEquals(SAMPLE_LOCAL, mapper.readValue(String.valueOf(millis), LocalDateTime.class));
                    assertEquals(SAMPLE_LOCAL, mapper.readValue("\"" + SAMPLE_LOCAL + "\"", LocalDateTime.class));
                });
    }

    @Test
    @DisplayName("JSON null 保持原可空性，不转换为 epoch 0")
    void jsonNull_保持可空性() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    assertNull(mapper.readValue("null", Instant.class));
                    assertNull(mapper.readValue("null", LocalDateTime.class));
                });
    }

    // ---------- 非法格式 fail-closed ----------

    @Test
    @DisplayName("非法格式拒绝：数组 / 秒小数 / 窗口外 / 无 offset ISO / 空字符串 / 错误类型")
    void 非法格式_一律拒绝() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    assertThrows(Exception.class, () -> mapper.readValue("[2026,10,14]", Instant.class));
                    assertThrows(Exception.class, () -> mapper.readValue("1769554000.123", Instant.class)); // 秒小数
                    assertThrows(Exception.class, () -> mapper.readValue("123", Instant.class)); // 非 10/13 位
                    assertThrows(Exception.class, () -> mapper.readValue("-1", Instant.class));
                    assertThrows(Exception.class, () -> mapper.readValue("0", Instant.class));
                    assertThrows(Exception.class, () -> mapper.readValue("\"2026-10-28T12:43:32\"", Instant.class)); // 缺 offset
                    assertThrows(Exception.class, () -> mapper.readValue("\"\"", Instant.class));
                    assertThrows(Exception.class, () -> mapper.readValue("true", Instant.class));
                    assertThrows(Exception.class, () -> mapper.readValue("\"1769554000000\"", Instant.class)); // 数字字符串
                });
    }

    // ---------- VSO 业务对象 wire 输出（FR-001-3 字段语义落地验证） ----------

    @Test
    @DisplayName("VSO 业务对象：Instant 字段输出绝对毫秒（SaleModel 生效区间/订单支付过期时间）")
    void Vso业务对象_Instant字段输出绝对毫秒() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);

                    // SaleModel 生效区间/审计时间（Instant 绝对时刻）
                    SaleModelResult saleModel = SaleModelResult.builder()
                            .saleModelCode("SMC001")
                            .effectiveFrom(SAMPLE_INSTANT)
                            .effectiveTo(SAMPLE_INSTANT.plusSeconds(3600))
                            .createTime(SAMPLE_INSTANT)
                            .modifyTime(SAMPLE_INSTANT)
                            .build();
                    JsonNode saleModelNode = mapper.valueToTree(saleModel);
                    assertEquals(SAMPLE_INSTANT.toEpochMilli(), saleModelNode.get("effectiveFrom").longValue());
                    assertEquals(SAMPLE_INSTANT.plusSeconds(3600).toEpochMilli(), saleModelNode.get("effectiveTo").longValue());
                    assertEquals(SAMPLE_INSTANT.toEpochMilli(), saleModelNode.get("createTime").longValue());

                    // 订单支付过期时间（Instant 绝对时刻）
                    EarnestMoneyOrderResult earnestResult = EarnestMoneyOrderResult.builder()
                            .orderNo("ORDER-001")
                            .expireTime(SAMPLE_INSTANT)
                            .build();
                    JsonNode earnestNode = mapper.valueToTree(earnestResult);
                    assertEquals(SAMPLE_INSTANT.toEpochMilli(), earnestNode.get("expireTime").longValue());
                });
    }

    @Test
    @DisplayName("VSO 业务对象：LocalDateTime 字段按 Asia/Shanghai 墙上时间输出毫秒（订单业务时间/配车时间）")
    void Vso业务对象_LocalDateTime字段按上海时区输出毫秒() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);

                    // 订单详情业务时间（LocalDateTime 墙上时间）
                    OrderDetailResponse orderDetail = new OrderDetailResponse();
                    orderDetail.setOrderId("ORDER-001");
                    orderDetail.setCreatedAtBusiness(SAMPLE_LOCAL);
                    orderDetail.setLockTime(SAMPLE_LOCAL.plusHours(1));
                    JsonNode orderNode = mapper.valueToTree(orderDetail);
                    assertEquals(SAMPLE_LOCAL.atZone(SHANGHAI).toInstant().toEpochMilli(),
                            orderNode.get("createdAtBusiness").longValue());
                    assertEquals(SAMPLE_LOCAL.plusHours(1).atZone(SHANGHAI).toInstant().toEpochMilli(),
                            orderNode.get("lockTime").longValue());

                    // 补充支付过期时间/创建时间（LocalDateTime）
                    SupplementaryPaymentVo supplementary = SupplementaryPaymentVo.builder()
                            .supplementaryNo("SP-001")
                            .expireTime(SAMPLE_LOCAL)
                            .createTime(SAMPLE_LOCAL)
                            .build();
                    JsonNode spNode = mapper.valueToTree(supplementary);
                    assertEquals(SAMPLE_LOCAL.atZone(SHANGHAI).toInstant().toEpochMilli(), spNode.get("expireTime").longValue());
                    assertEquals(SAMPLE_LOCAL.atZone(SHANGHAI).toInstant().toEpochMilli(), spNode.get("createTime").longValue());

                    // 配车时间（LocalDateTime）
                    VehicleAssignmentVo assignment = new VehicleAssignmentVo();
                    assignment.setAssignTime(SAMPLE_LOCAL);
                    JsonNode assignNode = mapper.valueToTree(assignment);
                    assertEquals(SAMPLE_LOCAL.atZone(SHANGHAI).toInstant().toEpochMilli(), assignNode.get("assignTime").longValue());
                });
    }

    // ---------- 排除项不变（FR-001-4） ----------

    @Test
    @DisplayName("排除项：支付回调入站双读 ISO/数字到同一时刻，协议字段结构不变")
    void 支付回调_入站双读_字段结构不变() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);

                    // ISO 入站
                    PaymentCallbackRequest isoRequest = mapper.readValue(
                            "{\"paymentNo\":\"PAY-001\",\"payTime\":\"2026-10-14T14:26:13\"}",
                            PaymentCallbackRequest.class);
                    assertEquals(SAMPLE_LOCAL, isoRequest.getPayTime());

                    // 旧数字入站（13 位毫秒）
                    long millis = SAMPLE_LOCAL.atZone(SHANGHAI).toInstant().toEpochMilli();
                    PaymentCallbackRequest numberRequest = mapper.readValue(
                            "{\"paymentNo\":\"PAY-002\",\"payTime\":" + millis + "}",
                            PaymentCallbackRequest.class);
                    assertEquals(SAMPLE_LOCAL, numberRequest.getPayTime());

                    // 字段结构保持（外部协议不含新字段）
                    assertNull(isoRequest.getSignature());
                    assertNull(isoRequest.getTimestamp());
                });
    }

    @Test
    @DisplayName("排除项：TimeoutTask Redis 持久载荷序列化/反序列化往返一致")
    void TimeoutTask_Redis往返一致() {
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    TimeoutTask task = new TimeoutTask("TASK-001", "ORDER-001", "LOCK_TIMEOUT", "remind", 2880);
                    task.setPlanTriggerTime(SAMPLE_LOCAL);
                    task.setActualTriggerTime(SAMPLE_LOCAL.plusMinutes(5));
                    task.setCreateTime(SAMPLE_LOCAL);

                    String json = mapper.writeValueAsString(task);
                    TimeoutTask roundTrip = mapper.readValue(json, TimeoutTask.class);
                    assertEquals(task.getTaskId(), roundTrip.getTaskId());
                    assertEquals(task.getTaskType(), roundTrip.getTaskType());
                    assertEquals(task.getPlanTriggerTime(), roundTrip.getPlanTriggerTime());
                    assertEquals(task.getActualTriggerTime(), roundTrip.getActualTriggerTime());
                    assertEquals(task.getCreateTime(), roundTrip.getCreateTime());
                });
    }

    @Test
    @DisplayName("排除项：签名/渠道外部协议不经过 JSON java.time（CallbackSignatureService 字符串签名）")
    void 签名协议_不受JSON时间契约影响() {
        // 签名输入为字典序字符串拼接（HMAC-SHA256），timestamp 为 Unix 秒 long，
        // 不经过 ObjectMapper；本测试仅确保受管 mapper 对 long 字段无时间转换。
        runner()
                .withPropertyValues("iov.web.jackson.date-format=epoch-millis", "iov.web.jackson.zone-id=Asia/Shanghai")
                .run(context -> {
                    ObjectMapper mapper = context.getBean(ObjectMapper.class);
                    PaymentCallbackRequest request = new PaymentCallbackRequest("PAY-001", null, "SMALL",
                            new java.math.BigDecimal("1000.00"), "SUCCESS", null, "idem-1", "sig", 1769554000L, "nonce-1");
                    JsonNode node = mapper.valueToTree(request);
                    assertEquals(1769554000L, node.get("timestamp").longValue());
                    assertEquals("sig", node.get("signature").asText());
                });
    }

    // ---------- 辅助 ----------

    static class TimeSample {
        public Instant instant;
        public OffsetDateTime offset;
        public LocalDateTime local;

        TimeSample() {
        }

        TimeSample(Instant instant, OffsetDateTime offset, LocalDateTime local) {
            this.instant = instant;
            this.offset = offset;
            this.local = local;
        }
    }
}
