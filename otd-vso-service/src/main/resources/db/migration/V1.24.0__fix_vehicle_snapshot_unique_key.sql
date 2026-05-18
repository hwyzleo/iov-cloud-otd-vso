ALTER TABLE vso_order_vehicle_snapshot DROP INDEX uk_order_snapshot_valid;

ALTER TABLE vso_order_vehicle_snapshot ADD UNIQUE INDEX uk_order_snapshot_version (order_id, snapshot_version);