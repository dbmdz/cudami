DROP TABLE IF EXISTS article_identifiables;
DROP TABLE IF EXISTS articles;

DROP TABLE IF EXISTS contenttree_contentnode;
DROP TABLE IF EXISTS contenttrees;

DROP TABLE IF EXISTS contentnode_identifiables;
DROP TABLE IF EXISTS contentnode_contentnode;
DROP TABLE IF EXISTS contentnodes;

DROP TABLE IF EXISTS digitalobject_fileresources;
DROP TABLE IF EXISTS digitalobjects;

DROP TABLE IF EXISTS entities;

DROP TABLE IF EXISTS fileresources_video;
DROP TABLE IF EXISTS fileresources_image;
DROP TABLE IF EXISTS fileresources_audio;
DROP TABLE IF EXISTS fileresources;

DROP TABLE IF EXISTS webpage_identifiables;
DROP TABLE IF EXISTS webpage_webpage;
DROP TABLE IF EXISTS website_webpage;
DROP TABLE IF EXISTS webpages;
DROP TABLE IF EXISTS websites;

DROP TABLE IF EXISTS identifiables;

ALTER TABLE IF EXISTS new_article_fileresources RENAME TO article_fileresources;
ALTER TABLE IF EXISTS new_articles RENAME TO articles;

ALTER TABLE IF EXISTS new_contentnode_contentnodes RENAME TO contentnode_contentnodes;
ALTER TABLE IF EXISTS new_contentnode_entities RENAME TO contentnode_entities;
ALTER TABLE IF EXISTS new_contentnode_fileresources RENAME TO contentnode_fileresources;
ALTER TABLE IF EXISTS new_contentnodes RENAME TO contentnodes;

ALTER TABLE IF EXISTS new_contenttree_contentnodes RENAME TO contenttree_contentnodes;
ALTER TABLE IF EXISTS new_contenttrees RENAME TO contenttrees;

ALTER TABLE IF EXISTS new_digitalobject_fileresources RENAME TO digitalobject_fileresources;
ALTER TABLE IF EXISTS new_digitalobjects RENAME TO digitalobjects;

ALTER TABLE IF EXISTS new_entities RENAME TO entities;

ALTER TABLE IF EXISTS new_fileresources RENAME TO fileresources;
ALTER TABLE IF EXISTS new_fileresources_audio RENAME TO fileresources_audio;
ALTER TABLE IF EXISTS new_fileresources_image RENAME TO fileresources_image;
ALTER TABLE IF EXISTS new_fileresources_video RENAME TO fileresources_video;

ALTER TABLE IF EXISTS new_identifiables RENAME TO identifiables;

ALTER TABLE IF EXISTS new_webpage_fileresources RENAME TO webpage_fileresources;
ALTER TABLE IF EXISTS new_webpage_webpages RENAME TO webpage_webpages;
ALTER TABLE IF EXISTS new_webpages RENAME TO webpages;

ALTER TABLE IF EXISTS new_website_webpages RENAME TO website_webpages;
ALTER TABLE IF EXISTS new_websites RENAME TO websites;