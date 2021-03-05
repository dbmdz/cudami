INSERT INTO rel_identifiable_entities(identifiable_uuid, entity_uuid, sortIndex)
SELECT p.entitypart_uuid as identifiable_uuid, p.entity_uuid as entity_uuid, p.sortIndex as sortIndex
FROM rel_entitypart_entities p;
