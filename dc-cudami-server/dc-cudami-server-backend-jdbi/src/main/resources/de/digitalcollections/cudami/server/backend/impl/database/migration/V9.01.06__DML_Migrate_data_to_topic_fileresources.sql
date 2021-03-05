INSERT INTO topic_fileresources(topic_uuid, fileresource_uuid, sortIndex)
SELECT
  sf.subtopic_uuid as topic_uuid,
  sf.fileresource_uuid as fileresource_uuid,
  sf.sortIndex as sortIndex
FROM subtopic_fileresources sf;
