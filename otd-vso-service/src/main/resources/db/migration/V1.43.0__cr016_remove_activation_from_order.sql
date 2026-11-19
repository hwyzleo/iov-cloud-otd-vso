-- ============================================================
-- VSO 车辆销售订单系统 - 删除激活状态并保守迁移历史 700（OTD-VSO-DSN-CR-016）
-- Flyway 版本：V1.43.0
-- 说明：仅数据 UPDATE 与核查 SELECT，不新增/删除表、字段或索引。
--       700 且同订单存在有效 ORDER_COMPLETE 时间线证据 → 900，其余 700 → 650。
--       历史状态快照（previous_order_state / 影子删除 before_order_state）中的 700 统一归一为 650。
--       不修改 vso_order_timeline 的任何字段，不补写 DELIVERED_TIMEOUT，不补发 vso.order.delivered，
--       不伪造 ORDER_COMPLETED。不提供 down migration；回滚代码时保留迁移后的 650/900 数据。
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 一、迁移前核查基线
-- ============================================================

SET @before_700_total = (
    SELECT COUNT(*) FROM `vso_order` WHERE `order_state` = 700
);

SET @before_700_with_complete = (
    SELECT COUNT(*)
    FROM `vso_order` o
    WHERE o.`order_state` = 700
      AND EXISTS (
          SELECT 1
          FROM `vso_order_timeline` t
          WHERE t.`order_id` = o.`order_id`
            AND t.`event_type` = 'ORDER_COMPLETE'
            AND t.`row_valid` = 1
      )
);

-- ============================================================
-- 二、主表状态迁移（保守归一）
-- ============================================================

-- 2.1 有 ORDER_COMPLETE 时间线证据 → 900：已办结
UPDATE `vso_order` o
    JOIN (
        SELECT DISTINCT t.`order_id`
        FROM `vso_order_timeline` t
        WHERE t.`event_type` = 'ORDER_COMPLETE'
          AND t.`row_valid` = 1
    ) e ON e.`order_id` = o.`order_id`
SET o.`order_state` = 900
WHERE o.`order_state` = 700;

-- 2.2 其余 700 → 650：已交付但无办结证据，交由运营重新办结
UPDATE `vso_order`
SET `order_state` = 650
WHERE `order_state` = 700;

-- ============================================================
-- 三、历史状态快照归一（不得仅凭激活推断完成）
-- ============================================================

UPDATE `vso_order`
SET `previous_order_state` = 650
WHERE `previous_order_state` = 700;

UPDATE `vso_order_shadow_delete`
SET `before_order_state` = 650
WHERE `before_order_state` = 700;

-- ============================================================
-- 四、迁移后核查：剩余 700 非零即阻断发布
-- ============================================================

SELECT 'cr016_migration_check' AS metric,
       @before_700_total AS before_700_total,
       @before_700_with_complete AS moved_to_900_with_evidence,
       (@before_700_total - @before_700_with_complete) AS moved_to_650_without_evidence,
       (SELECT COUNT(*) FROM `vso_order` WHERE `order_state` = 700) AS remaining_700,
       (SELECT COUNT(*) FROM `vso_order` WHERE `order_state` = 700) AS must_be_zero;
