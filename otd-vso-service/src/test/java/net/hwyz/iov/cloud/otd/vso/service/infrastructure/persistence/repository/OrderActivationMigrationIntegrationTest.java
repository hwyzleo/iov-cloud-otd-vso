package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 历史 700 迁移集成测试（OTD-VSO-DSN-CR-016 / INV-059～060）
 * <p>
 * 真实 MySQL 上执行 V1.43.0 脚本：有 ORDER_COMPLETE 证据→900，无证据→650，
 * 历史状态快照（previous_order_state / 影子删除 before_order_state）归一为 650，
 * 时间线不改写，剩余 700 为 0，且不产生任何自动事件或办结事实。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false",
        "vso.outbox.relay.enabled=false"
})
@DisplayName("V1.43.0 - 历史 700 保守迁移")
class OrderActivationMigrationIntegrationTest {

    private static final String SCRIPT = "db/migration/V1.43.0__cr016_remove_activation_from_order.sql";

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String suffix;
    private String idWithEvidence;
    private String idWithoutEvidence;
    private String idDeliveredOnly;
    private String idNotActivated;
    private String shadowDeleteId;

    @BeforeEach
    void setUp() {
        suffix = String.valueOf(System.nanoTime());
        idWithEvidence = "ORD_MIG_EVD_" + suffix;
        idWithoutEvidence = "ORD_MIG_NOE_" + suffix;
        idDeliveredOnly = "ORD_MIG_DLV_" + suffix;
        idNotActivated = "ORD_MIG_900_" + suffix;
        shadowDeleteId = "SD_MIG_" + suffix;

        insertOrder(idWithEvidence, 700, 700);
        insertOrder(idWithoutEvidence, 700, null);
        insertOrder(idDeliveredOnly, 700, null);
        insertOrder(idNotActivated, 650, null);

        insertTimeline("TL_EVD_" + suffix, idWithEvidence, "ORDER_COMPLETE", "650", "900");
        insertTimeline("TL_DLV_" + suffix, idDeliveredOnly, "ORDER_DELIVERED", "620", "650");
        insertTimeline("TL_900_" + suffix, idNotActivated, "ORDER_COMPLETE", "650", "900");

        jdbcTemplate.update("INSERT INTO vso_order_shadow_delete (shadow_delete_id, origin_order_no, "
                        + "before_order_state, compliance_delete_flag, row_version, row_valid) "
                        + "VALUES (?, ?, 700, 0, 0, 1)",
                shadowDeleteId, "SHADOW_" + suffix);
    }

    @AfterEach
    void cleanup() {
        for (String orderId : List.of(idWithEvidence, idWithoutEvidence, idDeliveredOnly, idNotActivated)) {
            jdbcTemplate.update("DELETE FROM vso_order_timeline WHERE order_id = ?", orderId);
            jdbcTemplate.update("DELETE FROM vso_delivery_record WHERE order_id = ?", orderId);
            jdbcTemplate.update("DELETE FROM vso_outbox WHERE aggregate_id = ?", orderId);
            jdbcTemplate.update("DELETE FROM vso_order WHERE order_id = ?", orderId);
        }
        jdbcTemplate.update("DELETE FROM vso_order_shadow_delete WHERE shadow_delete_id = ?", shadowDeleteId);
    }

    @Test
    @DisplayName("有完成证据→900；无证据→650；快照归一；时间线不改写；无自动事实")
    void 保守迁移历史700() throws Exception {
        int timelineBefore = countTimeline();
        List<Map<String, Object>> evidenceTimelineBefore = jdbcTemplate.queryForList(
                "SELECT event_type, before_status, after_status, event_remark FROM vso_order_timeline "
                        + "WHERE order_id = ?", idWithEvidence);

        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(SCRIPT));
        }

        assertThat(stateOf(idWithEvidence)).isEqualTo(900);
        assertThat(stateOf(idWithoutEvidence)).isEqualTo(650);
        assertThat(stateOf(idDeliveredOnly)).isEqualTo(650);
        assertThat(stateOf(idNotActivated)).isEqualTo(650);

        assertThat(previousStateOf(idWithEvidence)).isEqualTo(650);
        assertThat(shadowBeforeState()).isEqualTo(650);

        assertThat(countTimeline()).isEqualTo(timelineBefore);
        assertThat(jdbcTemplate.queryForList(
                "SELECT event_type, before_status, after_status, event_remark FROM vso_order_timeline "
                        + "WHERE order_id = ?", idWithEvidence)).isEqualTo(evidenceTimelineBefore);

        Integer remaining700 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vso_order WHERE row_valid = 1 AND order_state = 700 "
                        + "AND order_id LIKE ?", Integer.class, "ORD_MIG_%_" + suffix);
        assertThat(remaining700).isZero();

        assertThat(countByOrder("vso_delivery_record", idWithEvidence)).isZero();
        assertThat(countByOrder("vso_outbox", idWithEvidence)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vso_order_timeline WHERE order_id = ? AND event_type = 'ORDER_COMPLETED'",
                Integer.class, idWithEvidence)).isZero();
    }

    private void insertOrder(String orderId, Integer orderState, Integer previousOrderState) {
        jdbcTemplate.update("INSERT INTO vso_order (order_id, order_no, order_type, order_source, "
                        + "order_state, previous_order_state, brand_code, row_version, row_valid) "
                        + "VALUES (?, ?, 'formal', 'capp', ?, ?, 'OPENIOV', 0, 1)",
                orderId, "NO_" + orderId, orderState, previousOrderState);
    }

    private void insertTimeline(String timelineId, String orderId, String eventType,
                                String beforeStatus, String afterStatus) {
        jdbcTemplate.update("INSERT INTO vso_order_timeline (timeline_id, order_id, event_type, event_name, "
                        + "before_status, after_status, operator_id, operator_role, operate_source, "
                        + "result, event_remark, row_version, row_valid) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 'system', 'system', 'system', 'success', '历史事实', 0, 1)",
                timelineId, orderId, eventType, eventType, beforeStatus, afterStatus);
    }

    private Integer stateOf(String orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT order_state FROM vso_order WHERE order_id = ?", Integer.class, orderId);
    }

    private Integer previousStateOf(String orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT previous_order_state FROM vso_order WHERE order_id = ?", Integer.class, orderId);
    }

    private Integer shadowBeforeState() {
        return jdbcTemplate.queryForObject(
                "SELECT before_order_state FROM vso_order_shadow_delete WHERE shadow_delete_id = ?",
                Integer.class, shadowDeleteId);
    }

    private int countTimeline() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vso_order_timeline WHERE order_id LIKE ?",
                Integer.class, "ORD_MIG_%_" + suffix);
    }

    private int countByOrder(String table, String orderId) {
        String column = "vso_outbox".equals(table) ? "aggregate_id" : "order_id";
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?", Integer.class, orderId);
    }
}
