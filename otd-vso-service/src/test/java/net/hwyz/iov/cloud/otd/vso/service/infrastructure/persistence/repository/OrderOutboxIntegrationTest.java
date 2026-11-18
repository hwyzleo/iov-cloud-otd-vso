package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import net.hwyz.iov.cloud.otd.vso.service.application.service.TransportFulfillmentAppService;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 发件箱集成测试（OTD-VSO-DSN-CR-012：INV-030 / INV-031）
 * <p>
 * 真实 MySQL + Flyway 迁移：验证 vso_outbox 落库、载荷契约、业务幂等与「事务回滚不留残留」。
 * 不依赖 Kafka（中继在测试中关闭）。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false",
        "vso.outbox.relay.enabled=false"
})
@DisplayName("发件箱 - 同事务落库/幂等/回滚无残留")
class OrderOutboxIntegrationTest {

    private static final String EVENT_TYPE = "vso.transport.applied";

    @Autowired
    private TransportFulfillmentAppService transportFulfillmentAppService;
    @Autowired
    private OrderOutboxRepository orderOutboxRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private Order order(String aggregateId) {
        return Order.builder()
                .id(aggregateId)
                .orderNo("SO-" + aggregateId)
                .orderState(OrderState.APPLY_TRANSPORT)
                .deliveryVin("VIN" + aggregateId)
                .brandCode("HW")
                .saleModel("M01")
                .configurationCode("CFG-1")
                .deliveryStoreCode("D0001")
                .deliveryRegionCode("R01")
                .licenseCity("310000")
                .transportApplyPersonId("EMP001")
                .transportApplyPersonName("张三")
                .transportApplyTime(new Date())
                .currentVersionNo(3)
                .build();
    }

    private Map<String, Object> loadRow(String aggregateId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT event_id, event_type, aggregate_type, aggregate_id, aggregate_version, topic, message_key, "
                        + "payload, publish_state, retry_count FROM vso_outbox WHERE aggregate_id = ?", aggregateId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void cleanup(String aggregateId) {
        jdbcTemplate.update("DELETE FROM vso_outbox WHERE aggregate_id = ?", aggregateId);
    }

    @Test
    @DisplayName("提交后发件箱落库且载荷为不可变快照")
    void shouldPersistOutboxOnCommit() {
        String aggregateId = "IT-OUTBOX-COMMIT-1";
        cleanup(aggregateId);
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    transportFulfillmentAppService.appendTransportApplied(order(aggregateId)));

            Map<String, Object> row = loadRow(aggregateId);
            assertThat(row).isNotNull();
            assertThat(row.get("event_type")).isEqualTo(EVENT_TYPE);
            assertThat(row.get("aggregate_type")).isEqualTo("ORDER");
            assertThat(row.get("aggregate_version")).isEqualTo(3L);
            assertThat(row.get("topic")).isEqualTo("vso.transport.applied");
            assertThat(row.get("message_key")).isEqualTo("SO-" + aggregateId);
            assertThat(row.get("publish_state")).isEqualTo("PENDING");
            assertThat(((Number) row.get("retry_count")).intValue()).isZero();
            String payload = (String) row.get("payload");
            assertThat(payload).contains("\"schemaVersion\":1")
                    .contains("\"eventType\":\"vso.transport.applied\"")
                    .contains("\"orderNo\":\"SO-" + aggregateId + "\"");
            assertThat(payload).doesNotContain("orderPersonPhone").doesNotContain("orderPersonIdNum");
        } finally {
            cleanup(aggregateId);
        }
    }

    @Test
    @DisplayName("事务回滚时发件箱无残留（INV-030）")
    void shouldRollbackOutboxWithBusinessTransaction() {
        String aggregateId = "IT-OUTBOX-ROLLBACK-1";
        cleanup(aggregateId);
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                transportFulfillmentAppService.appendTransportApplied(order(aggregateId));
                status.setRollbackOnly();
            });

            assertThat(loadRow(aggregateId)).isNull();
        } finally {
            cleanup(aggregateId);
        }
    }

    @Test
    @DisplayName("业务幂等：同一聚合重复入箱只保留一条（INV-031）")
    void shouldAppendOnlyOnceForSameAggregate() {
        String aggregateId = "IT-OUTBOX-IDEMPOTENT-1";
        cleanup(aggregateId);
        try {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.executeWithoutResult(status ->
                    transportFulfillmentAppService.appendTransportApplied(order(aggregateId)));
            template.executeWithoutResult(status ->
                    transportFulfillmentAppService.appendTransportApplied(order(aggregateId)));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM vso_outbox WHERE aggregate_id = ? AND event_type = ?",
                    Integer.class, aggregateId, EVENT_TYPE);
            assertThat(count).isEqualTo(1);
            assertThat(orderOutboxRepository.existsByEventTypeAndAggregateId(EVENT_TYPE, aggregateId)).isTrue();
            assertThat(orderOutboxRepository.existsByEventTypeAndAggregateId(EVENT_TYPE, "NOT-EXIST")).isFalse();
        } finally {
            cleanup(aggregateId);
        }
    }
}
