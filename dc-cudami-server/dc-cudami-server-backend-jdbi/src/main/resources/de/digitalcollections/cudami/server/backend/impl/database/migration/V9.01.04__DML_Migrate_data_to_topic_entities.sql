INSERT INTO topic_entities(topic_uuid, entity_uuid, sortIndex)
SELECT
  se.subtopic_uuid as topic_uuid,
  se.entity_uuid as entity_uuid,
  se.sortIndex as sortIndex
FROM subtopic_entities se;
