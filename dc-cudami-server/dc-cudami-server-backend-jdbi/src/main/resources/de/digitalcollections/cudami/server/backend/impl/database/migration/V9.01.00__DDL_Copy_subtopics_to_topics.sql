INSERT INTO topics(uuid, created, description, identifiable_type, label, last_modified, entity_type, previewfileresource, preview_hints)
SELECT
  s.uuid as uuid,
  s.created as created,
  s.description as description,
  'ENTITY' as identifiable_type,
  s.label as label,
  s.last_modified as last_modified,
  'TOPIC' as entity_type,
  s.previewfileresource as previewfileresource,
  s.preview_hints as preview_hints
FROM subtopics s;
