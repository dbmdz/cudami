ALTER TABLE manifestations
  DROP CONSTRAINT IF EXISTS fk_parent;

DROP TRIGGER IF EXISTS tr_manifestations_involvements ON manifestations;
DROP TRIGGER IF EXISTS tr_manifestations_series ON manifestations;

ALTER TABLE manifestations
  ADD COLUMN IF NOT EXISTS manifestationtype varchar COLLATE "ucs_basic",
  DROP COLUMN IF EXISTS parent,
  ADD COLUMN IF NOT EXISTS publishing_timevaluerange jsonb,
  DROP COLUMN IF EXISTS series_uuids,
  DROP COLUMN IF EXISTS sortkey;

ALTER TABLE manifestations
  DROP CONSTRAINT IF EXISTS fk_previewfileresource,
  ADD CONSTRAINT fk_previewfileresource FOREIGN KEY (previewfileresource) REFERENCES fileresources_image;

