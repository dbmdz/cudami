CREATE INDEX IF NOT EXISTS url_aliases_target_uuid_asc_idx ON url_aliases (target_uuid);
-- Create indexes for coming default sorting: last_modified desc, uuid asc
CREATE INDEX IF NOT EXISTS digitalobjects_last_modified_desc_uuid_idx ON digitalobjects (last_modified DESC, uuid);
-- To create an index on a parent table the child tables must be indexed. Indexes are not inherited.
-- We only index the biggest table for now.
CREATE INDEX IF NOT EXISTS fileresources_image_last_modified_desc_uuid_idx ON fileresources_image (last_modified DESC, uuid);
ANALYZE;
