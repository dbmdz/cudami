ALTER TABLE items
ADD COLUMN IF NOT EXISTS exemplifies_manifestation boolean,
ADD COLUMN IF NOT EXISTS holder_uuids UUID[],
ADD COLUMN IF NOT EXISTS manifestation UUID,
ADD COLUMN IF NOT EXISTS part_of_item UUID;
