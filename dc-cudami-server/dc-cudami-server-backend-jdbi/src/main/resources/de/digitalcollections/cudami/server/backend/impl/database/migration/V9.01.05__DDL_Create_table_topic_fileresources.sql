CREATE TABLE IF NOT EXISTS topic_fileresources (
  topic_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL,
  sortIndex SMALLINT,

  PRIMARY KEY (topic_uuid, fileresource_uuid),
  FOREIGN KEY (topic_uuid) REFERENCES topics(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES fileresources(uuid)
);
