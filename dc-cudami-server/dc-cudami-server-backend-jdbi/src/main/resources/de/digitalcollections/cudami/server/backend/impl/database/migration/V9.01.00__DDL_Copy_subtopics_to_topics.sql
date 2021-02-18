INSERT INTO topics(uuid, created, description, label, last_modified, previewfileresource, preview_hints)
SELECT
  s.uuid as uuid,
  s.created as created,
  s.description as description,
  s.label as label,
  s.last_modified as last_modified,
  s.previewfileresource as previewfileresource,
  s.preview_hints as preview_hints
FROM subtopics s;

UPDATE topics SET identifiable_type='ENTITY', entity_type='TOPIC' WHERE entity_type IS NULL; 