INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex)
SELECT article_uuid, fileresource_uuid, sortindex
FROM article_fileresources;

DROP TABLE article_fileresources;

INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex)
SELECT webpage_uuid, fileresource_uuid, sortindex
FROM webpage_fileresources;

DROP TABLE webpage_fileresources;