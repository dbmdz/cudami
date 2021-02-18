CREATE TABLE IF NOT EXISTS topic_topics (
  parent_topic_uuid UUID NOT NULL,
  child_topic_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (parent_topic_uuid, child_topic_uuid),
  FOREIGN KEY (parent_topic_uuid) REFERENCES topics(uuid),
  FOREIGN KEY (child_topic_uuid) REFERENCES topics(uuid)
);
