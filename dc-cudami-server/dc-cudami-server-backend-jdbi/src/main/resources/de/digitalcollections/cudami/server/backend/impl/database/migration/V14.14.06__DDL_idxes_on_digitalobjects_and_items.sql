CREATE INDEX IF NOT EXISTS idx_digitalobjects_item_hash on digitalobjects using hash (item_uuid);
CREATE INDEX IF NOT EXISTS idx_items_manifestation_hash on items using hash (manifestation);

