ALTER TABLE website_webpage ADD COLUMN sortIndex SMALLINT;
UPDATE website_webpage SET sortIndex = (ctid::text::point)[1]::smallint;