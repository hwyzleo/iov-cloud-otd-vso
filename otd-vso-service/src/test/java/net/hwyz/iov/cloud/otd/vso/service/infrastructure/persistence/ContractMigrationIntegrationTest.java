package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * V1.42.0 数据库迁移测试（OTD-VSO-DSN-CR-014 §4.2 / §4.3 / INV-047）
 * <p>
 * 断言 sign_no 列与 idx_contract_sign_no 索引存在；尾款回填只命中白名单
 * （全款 + 非小定升级 + 尾款为 0 + 净应收 > 定金），异常行保持原值。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("V1.42.0 迁移 - sign_no 与尾款白名单回填")
class ContractMigrationIntegrationTest {

    private static final String BACKFILL_SQL = """
            UPDATE vso_order_amount a
                JOIN vso_order o ON o.order_id = a.order_id AND o.row_valid = 1
            SET a.tail_payment_amount = a.net_receivable_total - a.down_payment_amount
            WHERE a.row_valid = 1
              AND o.payment_method = 'full_payment'
              AND a.upgraded_from_small = 0
              AND a.tail_payment_amount = 0
              AND a.net_receivable_total > a.down_payment_amount
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long suffix;

    @BeforeEach
    void setUp() {
        suffix = System.nanoTime();
    }

    private int count(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private void insertOrder(String orderId, String paymentMethod) {
        jdbcTemplate.update("INSERT INTO vso_order (order_id, order_no, order_type, order_source, "
                        + "payment_method, order_state, row_valid) VALUES (?, ?, 'formal', 'capp', ?, 620, 1)",
                orderId, "SON" + orderId, paymentMethod);
    }

    private void insertAmount(String orderId, String down, String net, String tail, int upgradedFromSmall) {
        jdbcTemplate.update("INSERT INTO vso_order_amount (amount_id, order_id, down_payment_amount, "
                        + "net_receivable_total, tail_payment_amount, upgraded_from_small, row_valid) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 1)",
                "AMT" + orderId, orderId, new BigDecimal(down), new BigDecimal(net), new BigDecimal(tail),
                upgradedFromSmall);
    }

    private BigDecimal tailOf(String orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT tail_payment_amount FROM vso_order_amount WHERE order_id = ? AND row_valid = 1",
                BigDecimal.class, orderId);
    }

    @Test
    @DisplayName("sign_no 列与 idx_contract_sign_no 索引存在")
    void signNo列与索引存在() {
        assertEquals(1, count("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME = 'vso_contract' AND COLUMN_NAME = 'sign_no'"));
        assertEquals(1, count("SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME = 'vso_contract' AND INDEX_NAME = 'idx_contract_sign_no'"));
    }

    @Test
    @DisplayName("白名单回填：全款且尾款为 0 且 net>down → 回填 183000")
    void 合格快照回填() {
        String orderId = "OK" + suffix;
        insertOrder(orderId, "full_payment");
        insertAmount(orderId, "20000.00", "203000.00", "0.00", 0);

        jdbcTemplate.update(BACKFILL_SQL);

        assertEquals(0, new BigDecimal("183000.00").compareTo(tailOf(orderId)));
    }

    @Test
    @DisplayName("贷款订单不回填")
    void 贷款订单不回填() {
        String orderId = "LOAN" + suffix;
        insertOrder(orderId, "loan");
        insertAmount(orderId, "20000.00", "203000.00", "0.00", 0);

        jdbcTemplate.update(BACKFILL_SQL);

        assertEquals(0, BigDecimal.ZERO.compareTo(tailOf(orderId)));
    }

    @Test
    @DisplayName("小定升级订单不回填")
    void 小定升级不回填() {
        String orderId = "UPG" + suffix;
        insertOrder(orderId, "full_payment");
        insertAmount(orderId, "20000.00", "203000.00", "0.00", 1);

        jdbcTemplate.update(BACKFILL_SQL);

        assertEquals(0, BigDecimal.ZERO.compareTo(tailOf(orderId)));
    }

    @Test
    @DisplayName("净应收小于定金不回填（异常行保持原值）")
    void 净应收小于定金不回填() {
        String orderId = "NEG" + suffix;
        insertOrder(orderId, "full_payment");
        insertAmount(orderId, "200000.00", "150000.00", "0.00", 0);

        jdbcTemplate.update(BACKFILL_SQL);

        assertEquals(0, BigDecimal.ZERO.compareTo(tailOf(orderId)));
    }

    @Test
    @DisplayName("已有非零尾款不被覆盖")
    void 已有尾款不覆盖() {
        String orderId = "TAILED" + suffix;
        insertOrder(orderId, "full_payment");
        insertAmount(orderId, "20000.00", "203000.00", "12345.00", 0);

        jdbcTemplate.update(BACKFILL_SQL);

        assertEquals(0, new BigDecimal("12345.00").compareTo(tailOf(orderId)));
    }
}
