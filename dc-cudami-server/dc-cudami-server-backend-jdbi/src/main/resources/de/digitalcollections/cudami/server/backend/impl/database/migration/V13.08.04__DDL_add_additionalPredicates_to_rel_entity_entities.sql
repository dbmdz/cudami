ALTER TABLE rel_entity_entities
  ADD COLUMN IF NOT EXISTS additional_predicates varchar[] COLLATE "ucs_basic";

