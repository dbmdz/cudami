ALTER TABLE contentnode_contentnode ADD COLUMN sortIndex SMALLINT;
UPDATE contentnode_contentnode SET sortIndex = (ctid::text::point)[1]::smallint;