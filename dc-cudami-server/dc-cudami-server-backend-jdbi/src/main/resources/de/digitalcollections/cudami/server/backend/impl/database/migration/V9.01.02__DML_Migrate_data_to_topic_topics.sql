INSERT INTO topic_topics(parent_topic_uuid, child_topic_uuid, sortIndex)
SELECT
  ts.topic_uuid as parent_topic_uuid,
  ts.subtopic_uuid as child_topic_uuid,
  ts.sortIndex as sortIndex
FROM topic_subtopics ts;

INSERT INTO topic_topics(parent_topic_uuid, child_topic_uuid, sortIndex)
SELECT
  ss.parent_subtopic_uuid as parent_topic_uuid,
  ss.child_subtopic_uuid as child_topic_uuid,
  ss.sortIndex as sortIndex
FROM subtopic_subtopics ss;