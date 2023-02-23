CREATE TABLE IF NOT EXISTS events (
  LIKE entities INCLUDING ALL,
  name jsonb NOT NULL,
  name_locales_original_scripts varchar[] COLLATE "ucs_basic",
  split_name varchar[]
) INHERITS (entities);

ALTER TABLE events ADD CONSTRAINT fk_events_previewfileresource_fileresources_image_uuid FOREIGN KEY (previewfileresource) REFERENCES fileresources_image (uuid);

CREATE TRIGGER tr_events_tags_uuids
BEFORE INSERT OR UPDATE
ON events
FOR EACH ROW
EXECUTE FUNCTION check_array_uuids_exist('tags', 'tags_uuids');

CREATE INDEX IF NOT EXISTS idx_events_split_label ON events USING GIN (split_label);
CREATE INDEX IF NOT EXISTS idx_events_split_name ON events USING GIN (split_name);
