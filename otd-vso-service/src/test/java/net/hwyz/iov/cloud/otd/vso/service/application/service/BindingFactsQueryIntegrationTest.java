package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingDeliveryStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingEvidenceType;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingLookupStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingReasonCode;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.BindingFactsQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.BindingFactsResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 绑车事实查询集成测试（OTD-VSO-DSN-CR-017，覆盖 AT-02～07、AT-10、AT-11）
 * <p>
 * 在本地真实库事务内验证账号/VIN 精确关联、角色、交付口径、歧义与只读语义；
 * 不代表真实跨服务 E2E，TSP 联调未执行。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("绑车事实查询 - 集成测试")
class BindingFactsQueryIntegrationTest {

    private static final String ACCOUNT = "BF-ACC-A";
    private static final String OTHER_ACCOUNT = "BF-ACC-B";
    private static final String VIN = "LSVBF1234K2100001";
    private static final String OTHER_VIN = "LSVBF1234K2100002";

    @Autowired
    private BindingFactsQueryAppService bindingFactsQueryAppService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderPartyRepository orderPartyRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ConfigurationService configurationService;

    private String suffix;
    private int sequence;

    @BeforeEach
    void setUp() {
        suffix = String.valueOf(System.nanoTime());
        sequence = 0;
    }

    @Test
    @DisplayName("650/900 有完整交付证据均为 DELIVERED，650→900 不改变交付引用")
    void 已交付口径() {
        String orderId = saveOrder(OrderState.DELIVERED, VIN);
        saveParty(orderId, ACCOUNT, "order_user");
        saveDeliveryRecord("DR-" + suffix, orderId, "delivered", VIN, LocalDateTime.now().withNano(0));

        BindingFactsResult delivered = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        assertThat(delivered.getLookupStatus()).isEqualTo(BindingLookupStatus.MATCHED);
        BindingFactsResult.Fact fact = delivered.getFacts().get(0);
        assertThat(fact.getDeliveryStatus()).isEqualTo(BindingDeliveryStatus.DELIVERED);
        String recordId = fact.getDeliveryRecordId();
        java.time.Instant actualDeliveryTime = fact.getActualDeliveryTime();
        assertThat(recordId).isNotNull();
        assertThat(actualDeliveryTime).isNotNull();

        // 650 → 900：交付事实引用与实际交付时间不变
        Order order = orderRepository.findByOrderId(orderId).orElseThrow();
        orderRepository.save(Order.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("OPENIOV")
                .orderState(OrderState.COMPLETED)
                .hasException(false)
                .currentVersionNo(order.getCurrentVersionNo())
                .lockedFlag(false)
                .reopenFlag(false)
                .deliveryStoreCode("STORE001")
                .deliveryVin(VIN)
                .build());

        BindingFactsResult completed = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        BindingFactsResult.Fact completedFact = completed.getFacts().get(0);
        assertThat(completedFact.getDeliveryStatus()).isEqualTo(BindingDeliveryStatus.DELIVERED);
        assertThat(completedFact.getDeliveryRecordId()).isEqualTo(recordId);
        assertThat(completedFact.getActualDeliveryTime()).isEqualTo(actualDeliveryTime);
    }

    @Test
    @DisplayName("他人账号订单统一 NO_MATCH；同账号其他 VIN 不匹配")
    void 账号与VIN隔离() {
        String otherOrderId = saveOrder(OrderState.DELIVERED, VIN);
        saveParty(otherOrderId, OTHER_ACCOUNT, "order_user");

        BindingFactsResult otherAccount = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        assertThat(otherAccount.getLookupStatus()).isEqualTo(BindingLookupStatus.NO_MATCH);
        assertThat(otherAccount.getReasonCode()).isEqualTo(BindingReasonCode.NO_ACCOUNT_VIN_MATCH);
        assertThat(otherAccount.getFacts()).isEmpty();

        String ownOrderId = saveOrder(OrderState.PREPARE_DELIVER, OTHER_VIN);
        saveParty(ownOrderId, ACCOUNT, "order_user");

        BindingFactsResult otherVin = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        assertThat(otherVin.getLookupStatus()).isEqualTo(BindingLookupStatus.NO_MATCH);
    }

    @Test
    @DisplayName("order_user/buyer/delivery_contact 分别匹配并保留原始角色，重复角色行不放大订单数")
    void 角色保留与去重() {
        String orderId = saveOrder(OrderState.PREPARE_DELIVER, VIN);
        saveParty(orderId, ACCOUNT, "order_user");
        saveParty(orderId, ACCOUNT, "delivery_contact");
        saveParty(orderId, ACCOUNT, "buyer");
        saveParty(orderId, ACCOUNT, "buyer");
        // 非绑车关联角色不构成候选
        saveParty(orderId, OTHER_ACCOUNT, "invoice_contact");

        BindingFactsResult result = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.MATCHED);
        assertThat(result.getFacts()).hasSize(1);
        BindingFactsResult.Fact fact = result.getFacts().get(0);
        assertThat(fact.getMatchedRoles()).containsExactlyInAnyOrder("order_user", "buyer", "delivery_contact");
        assertThat(fact.getDeliveryStatus()).isEqualTo(BindingDeliveryStatus.NOT_DELIVERED);
    }

    @Test
    @DisplayName("620 无交付记录为 NOT_DELIVERED；仅预约不能变成已交付")
    void 交付前状态不按预约推定() {
        String orderId = saveOrder(OrderState.FINAL_PAYMENT_PAID, VIN);
        saveParty(orderId, ACCOUNT, "order_user");
        jdbcTemplate.update("INSERT INTO vso_delivery_appointment (delivery_appointment_id, delivery_appointment_no,"
                        + " order_id, appointment_status, delivery_mode, appointment_time, create_time, modify_time,"
                        + " row_version, row_valid) VALUES (?,?,?,?,?,?,now(),now(),1,1)",
                "DA-" + suffix, "DANO-" + suffix, orderId, "booked", "delivery_center",
                LocalDateTime.now().withNano(0));

        BindingFactsResult result = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));

        BindingFactsResult.Fact fact = result.getFacts().get(0);
        assertThat(fact.getDeliveryStatus()).isEqualTo(BindingDeliveryStatus.NOT_DELIVERED);
        assertThat(fact.getDeliveryRecordId()).isNull();
        assertThat(fact.getActualDeliveryTime()).isNull();
    }

    @Test
    @DisplayName("650 缺记录为 INSUFFICIENT；记录 VIN 不一致为 INCONSISTENT")
    void 交付证据不足与冲突() {
        String missingRecordOrderId = saveOrder(OrderState.DELIVERED, VIN);
        saveParty(missingRecordOrderId, ACCOUNT, "order_user");

        BindingFactsResult insufficient = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        assertThat(insufficient.getLookupStatus()).isEqualTo(BindingLookupStatus.INSUFFICIENT);
        assertThat(insufficient.getReasonCode()).isEqualTo(BindingReasonCode.MISSING_DELIVERY_EVIDENCE);

        String mismatchedOrderId = saveOrder(OrderState.DELIVERED, OTHER_VIN);
        saveParty(mismatchedOrderId, ACCOUNT, "order_user");
        saveDeliveryRecord("DR-MIS-" + suffix, mismatchedOrderId, "delivered", VIN, LocalDateTime.now().withNano(0));

        BindingFactsResult inconsistent = bindingFactsQueryAppService.query(query(ACCOUNT, OTHER_VIN));
        assertThat(inconsistent.getLookupStatus()).isEqualTo(BindingLookupStatus.INCONSISTENT);
        assertThat(inconsistent.getReasonCode()).isEqualTo(BindingReasonCode.VIN_EVIDENCE_MISMATCH);
    }

    @Test
    @DisplayName("多订单候选为 AMBIGUOUS；历史 700 不得返回 DELIVERED")
    void 歧义与历史状态() {
        String firstOrderId = saveOrder(OrderState.PREPARE_DELIVER, VIN);
        saveParty(firstOrderId, ACCOUNT, "order_user");
        String secondOrderId = saveOrder(OrderState.FINAL_PAYMENT_PAID, VIN);
        saveParty(secondOrderId, ACCOUNT, "buyer");

        BindingFactsResult ambiguous = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        assertThat(ambiguous.getLookupStatus()).isEqualTo(BindingLookupStatus.AMBIGUOUS);
        assertThat(ambiguous.getReasonCode()).isEqualTo(BindingReasonCode.MULTIPLE_ORDER_MATCHES);
        assertThat(ambiguous.getFacts()).isEmpty();

        String legacyOrderId = saveOrderWithRawState(700, OTHER_VIN);
        saveParty(legacyOrderId, ACCOUNT, "order_user");

        BindingFactsResult legacy = bindingFactsQueryAppService.query(query(ACCOUNT, OTHER_VIN));
        assertThat(legacy.getLookupStatus()).isEqualTo(BindingLookupStatus.INSUFFICIENT);
        assertThat(legacy.getReasonCode()).isEqualTo(BindingReasonCode.UNSUPPORTED_LEGACY_STATE);
        assertThat(legacy.getFacts()).isEmpty();
    }

    @Test
    @DisplayName("查询前后业务表、时间线与 Outbox 无写入，重复调用幂等")
    void 只读且可重复调用() {
        String orderId = saveOrder(OrderState.DELIVERED, VIN);
        saveParty(orderId, ACCOUNT, "order_user");
        saveDeliveryRecord("DR-RO-" + suffix, orderId, "delivered", VIN, LocalDateTime.now().withNano(0));

        List<Map<String, Object>> before = snapshot(orderId);
        BindingFactsResult first = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        BindingFactsResult second = bindingFactsQueryAppService.query(query(ACCOUNT, VIN));
        List<Map<String, Object>> after = snapshot(orderId);

        assertThat(before).isEqualTo(after);
        assertThat(first.getFacts().get(0).getOrderId()).isEqualTo(second.getFacts().get(0).getOrderId());
        assertThat(first.getFacts().get(0).getDeliveryRecordId())
                .isEqualTo(second.getFacts().get(0).getDeliveryRecordId());
        assertThat(first.getFacts().get(0).getEvidenceRefs()).extracting(BindingFactsResult.EvidenceRef::getId)
                .isEqualTo(second.getFacts().get(0).getEvidenceRefs().stream()
                        .map(BindingFactsResult.EvidenceRef::getId).toList());
    }

    private List<Map<String, Object>> snapshot(String orderId) {
        return List.of(
                jdbcTemplate.queryForMap("SELECT order_state, row_version, modify_time FROM vso_order WHERE order_id = ?",
                        orderId),
                jdbcTemplate.queryForMap("SELECT COUNT(*) AS c, COALESCE(SUM(row_version),0) AS v"
                        + " FROM vso_order_party WHERE order_id = ?", orderId),
                jdbcTemplate.queryForMap("SELECT COUNT(*) AS c, COALESCE(SUM(row_version),0) AS v"
                        + " FROM vso_delivery_record WHERE order_id = ?", orderId),
                jdbcTemplate.queryForMap("SELECT COUNT(*) AS c FROM vso_order_timeline WHERE order_id = ?", orderId),
                jdbcTemplate.queryForMap("SELECT COUNT(*) AS c FROM vso_outbox WHERE aggregate_id = ?", orderId));
    }

    private BindingFactsQuery query(String accountId, String vin) {
        return BindingFactsQuery.builder().accountId(accountId).vin(vin).build();
    }

    private String saveOrder(OrderState state, String vin) {
        String orderKey = nextKey();
        String orderId = "ORD_BF_" + orderKey;
        orderRepository.save(Order.builder()
                .id(orderId)
                .orderNo("SO_BF_" + orderKey)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("OPENIOV")
                .orderState(state)
                .hasException(false)
                .currentVersionNo(1)
                .lockedFlag(false)
                .reopenFlag(false)
                .deliveryStoreCode("STORE001")
                .deliveryVin(vin)
                .build());
        return orderId;
    }

    private String saveOrderWithRawState(int rawState, String vin) {
        String orderKey = "LEGACY_" + nextKey();
        String orderId = "ORD_BF_" + orderKey;
        orderRepository.save(Order.builder()
                .id(orderId)
                .orderNo("SO_BF_" + orderKey)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("OPENIOV")
                .orderState(OrderState.DELIVERED)
                .hasException(false)
                .currentVersionNo(1)
                .lockedFlag(false)
                .reopenFlag(false)
                .deliveryStoreCode("STORE001")
                .deliveryVin(vin)
                .build());
        jdbcTemplate.update("UPDATE vso_order SET order_state = ? WHERE order_id = ?", rawState, orderId);
        return orderId;
    }

    private void saveParty(String orderId, String userId, String role) {
        OrderPartyPo po = new OrderPartyPo();
        po.setPartyId("P_BF_" + nextKey());
        po.setOrderId(orderId);
        po.setPartyRole(role);
        po.setUserId(userId);
        po.setName("测试主体");
        po.setAuthorizedFlag(0);
        orderPartyRepository.save(po);
    }

    private void saveDeliveryRecord(String recordId, String orderId, String status, String vin,
                                    LocalDateTime actualDeliveryTime) {
        deliveryRepository.saveRecord(DeliveryRecord.builder()
                .deliveryRecordId(recordId)
                .orderId(orderId)
                .deliveryStatus(status)
                .deliveryMode("delivery_center")
                .actualDeliveryTime(actualDeliveryTime)
                .deliveryStoreCode("STORE001")
                .vehicleVin(vin)
                .build());
    }

    private String nextKey() {
        return suffix + "_" + (++sequence);
    }
}
