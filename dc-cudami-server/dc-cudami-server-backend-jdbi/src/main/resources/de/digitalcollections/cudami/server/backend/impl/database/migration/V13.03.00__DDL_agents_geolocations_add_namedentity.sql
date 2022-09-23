ALTER TABLE agents
  ADD COLUMN IF NOT EXISTS name jsonb,
  ADD COLUMN IF NOT EXISTS name_locales_original_scripts varchar[] COLLATE "ucs_basic",
  ADD COLUMN IF NOT EXISTS split_name varchar[];

ALTER TABLE geolocations
  ADD COLUMN IF NOT EXISTS name jsonb,
  ADD COLUMN IF NOT EXISTS name_locales_original_scripts varchar[] COLLATE "ucs_basic",
  ADD COLUMN IF NOT EXISTS split_name varchar[];

UPDATE agents SET name = label,
  split_name = split_label;

UPDATE geolocations SET name = label,
  split_name = split_label,
  name_locales_original_scripts = (SELECT ARRAY[a] FROM jsonb_object_keys(label) t(a) LIMIT 1);

