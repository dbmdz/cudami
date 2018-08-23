ALTER TABLE webpage_webpage ADD COLUMN sortIndex SMALLINT;
UPDATE webpage_webpage SET sortIndex = (ctid::text::point)[1]::smallint;