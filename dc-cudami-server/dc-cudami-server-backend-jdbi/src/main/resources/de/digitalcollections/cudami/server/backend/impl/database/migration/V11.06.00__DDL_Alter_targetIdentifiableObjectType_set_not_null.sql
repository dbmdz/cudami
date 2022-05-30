ALTER TABLE url_aliases DISABLE TRIGGER tr_url_aliases_target_uuid;
ALTER TABLE url_aliases ALTER COLUMN target_identifiable_objecttype SET NOT NULL;
ALTER TABLE url_aliases ENABLE TRIGGER tr_url_aliases_target_uuid;