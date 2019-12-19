-- fix too short varchar(512) for long urls
ALTER TABLE fileresources ALTER COLUMN uri TYPE TEXT;
