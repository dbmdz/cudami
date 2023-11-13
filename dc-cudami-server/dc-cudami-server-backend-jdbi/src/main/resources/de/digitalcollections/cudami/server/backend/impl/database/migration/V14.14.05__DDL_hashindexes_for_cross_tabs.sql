CREATE INDEX IF NOT EXISTS idx_manifestation_manifestations_subj_hash ON manifestation_manifestations USING hash (subject_uuid);
CREATE INDEX IF NOT EXISTS idx_manifestation_manifestations_obj_hash ON manifestation_manifestations USING hash (object_uuid);

CREATE INDEX IF NOT EXISTS idx_rel_entity_entities_subj_hash ON rel_entity_entities USING hash (subject_uuid);
CREATE INDEX IF NOT EXISTS idx_rel_entity_entities_obj_hash ON rel_entity_entities USING hash (object_uuid);

