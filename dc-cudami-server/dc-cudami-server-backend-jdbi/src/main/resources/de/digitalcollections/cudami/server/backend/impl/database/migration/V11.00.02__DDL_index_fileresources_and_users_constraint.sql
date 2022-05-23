-- Add index on filename column in image file resources table
CREATE INDEX IF NOT EXISTS fileresources_image_filename_idx ON fileresources_image (filename);

-- Remove unnecessary unique constraint on users table
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_uuid_key;
