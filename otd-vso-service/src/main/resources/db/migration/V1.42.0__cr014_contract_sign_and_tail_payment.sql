-- ============================================================
-- VSO 车辆销售订单系统 - 购车合同 Mock 电子签与全款尾款支付（OTD-VSO-DSN-CR-014）
-- Flyway 版本：V1.42.0
-- 说明：增量新增 sign_no 与普通索引；按 INV-047 白名单受控回填尾款快照。
--       不提供破坏性 down migration；回滚代码时保留 sign_no 与已回填金额事实。
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 一、vso_contract 合同签署任务号
-- ============================================================

ALTER TABLE `vso_contract`
    ADD COLUMN `sign_no` VARCHAR(64) NULL DEFAULT NULL COMMENT '电子签当前签署任务号' AFTER `external_contract_no`;

ALTER TABLE `vso_contract`
    ADD INDEX `idx_contract_sign_no` (`sign_no`);

-- ============================================================
-- 二、vso_order_amount 尾款快照白名单回填（INV-047）
-- 仅回填：有效订单 + 全款 + 非小定升级 + 当前尾款为 0 + 净应收 > 定金
-- 不更新 paid_total / refund_total / unpaid_total，不读取车型或策略现价。
-- ============================================================

UPDATE `vso_order_amount` a
    JOIN `vso_order` o ON o.order_id = a.order_id AND o.row_valid = 1
SET a.tail_payment_amount = a.net_receivable_total - a.down_payment_amount
WHERE a.row_valid = 1
  AND o.payment_method = 'full_payment'
  AND a.upgraded_from_small = 0
  AND a.tail_payment_amount = 0
  AND a.net_receivable_total > a.down_payment_amount;
