ALTER TABLE tags ADD COLUMN IF NOT EXISTS value varchar COLLATE "ucs_basic";

UPDATE tags SET value = format('%s:%s:%s', "type", namespace, id);

DROP INDEX IF EXISTS idx_tags_split_label;
ALTER TABLE tags
  DROP CONSTRAINT IF EXISTS tags_type_namespace_id_key,
  DROP COLUMN IF EXISTS id CASCADE,
  DROP COLUMN IF EXISTS label CASCADE,
  DROP COLUMN IF EXISTS namespace CASCADE,
  DROP COLUMN IF EXISTS type CASCADE,
  DROP COLUMN IF EXISTS split_label;

ALTER TABLE tags
  ALTER COLUMN value SET NOT NULL,
  ADD CONSTRAINT unique_value UNIQUE (value);

