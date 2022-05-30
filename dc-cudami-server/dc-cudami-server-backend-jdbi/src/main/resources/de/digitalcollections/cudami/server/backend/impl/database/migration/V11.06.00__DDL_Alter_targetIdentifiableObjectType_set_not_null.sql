ALTER TABLE url_aliases DISABLE TRIGGER ALL;
ALTER TABLE url_aliases ALTER COLUMN target_identifiable_objecttype SET NOT NULL;
ALTER TABLE url_aliases ENABLE TRIGGER ALL;

