ALTER TABLE contenttree_contentnode ADD COLUMN sortIndex SMALLINT;
UPDATE contenttree_contentnode SET sortIndex = (ctid::text::point)[1]::smallint;