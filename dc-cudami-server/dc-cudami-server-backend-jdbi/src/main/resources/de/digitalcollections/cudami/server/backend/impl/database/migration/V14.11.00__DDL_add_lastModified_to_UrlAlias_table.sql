ALTER TABLE url_aliases
ADD COLUMN IF NOT EXISTS last_modified TIMESTAMP;
UPDATE url_aliases SET last_modified = created;
ALTER TABLE url_aliases ALTER COLUMN last_modified SET NOT NULL;