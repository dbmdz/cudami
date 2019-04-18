DELETE FROM new_article_fileresources;
DELETE FROM new_contentnode_contentnodes;
DELETE FROM new_contentnode_fileresources;
DELETE FROM new_contenttree_contentnodes;
DELETE FROM new_digitalobject_fileresources;
DELETE FROM new_webpage_fileresources;
DELETE FROM new_webpage_webpages;
DELETE FROM new_website_webpages;

DELETE FROM new_articles;
DELETE FROM new_contentnodes;
DELETE FROM new_contenttrees;
DELETE FROM new_digitalobjects;
DELETE FROM new_fileresources_audio;
DELETE FROM new_fileresources_image;
DELETE FROM new_fileresources_video;
DELETE FROM new_webpages;
DELETE FROM new_websites;

-- single object tables

INSERT INTO new_articles(uuid, created, description, identifiable_type, label, last_modified, entity_type, text)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, e.entity_type as entity_type, a.text as text
FROM articles a INNER JOIN entities e ON a.uuid=e.uuid INNER JOIN identifiables i ON a.uuid=i.uuid;

INSERT INTO new_contentnodes(uuid, created, description, identifiable_type, label, last_modified)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified
FROM contentnodes c INNER JOIN identifiables i ON c.uuid=i.uuid;

INSERT INTO new_contenttrees(uuid, created, description, identifiable_type, label, last_modified, entity_type)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, e.entity_type as entity_type
FROM contenttrees c INNER JOIN entities e ON c.uuid=e.uuid INNER JOIN identifiables i ON c.uuid=i.uuid;

INSERT INTO new_digitalobjects(uuid, created, description, identifiable_type, label, last_modified, entity_type)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, e.entity_type as entity_type
FROM digitalobjects d INNER JOIN entities e ON d.uuid=e.uuid INNER JOIN identifiables i ON d.uuid=i.uuid;

INSERT INTO new_fileresources_audio(uuid, created, description, identifiable_type, label, last_modified, filename, mimetype, size_in_bytes, uri, duration)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, f.filename as filename, f.mimetype as mimetype, f.size_in_bytes as size_in_bytes, f.uri as uri, f.duration as duration
FROM fileresources_audio f INNER JOIN identifiables i ON f.uuid=i.uuid;

INSERT INTO new_fileresources_image(uuid, created, description, identifiable_type, label, last_modified, filename, mimetype, size_in_bytes, uri, height, width)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, f.filename as filename, f.mimetype as mimetype, f.size_in_bytes as size_in_bytes, f.uri as uri, f.height as height, f.width as width
FROM fileresources_image f INNER JOIN identifiables i ON f.uuid=i.uuid;

INSERT INTO new_fileresources_video(uuid, created, description, identifiable_type, label, last_modified, filename, mimetype, size_in_bytes, uri, duration)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, f.filename as filename, f.mimetype as mimetype, f.size_in_bytes as size_in_bytes, f.uri as uri, f.duration as duration
FROM fileresources_video f INNER JOIN identifiables i ON f.uuid=i.uuid;

INSERT INTO new_webpages(uuid, created, description, identifiable_type, label, last_modified, text)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, w.text as text
FROM webpages w INNER JOIN identifiables i ON w.uuid=i.uuid;

INSERT INTO new_websites(uuid, created, description, identifiable_type, label, last_modified, entity_type, url, registration_date)
SELECT i.uuid as uuid, i.created as created, i.description as description, i.identifiable_type as identifiable_type, i.label as label, i.last_modified as last_modified, e.entity_type as entity_type, w.url as url, w.registration_date as registration_date
FROM websites w INNER JOIN entities e ON w.uuid=e.uuid INNER JOIN identifiables i ON w.uuid=i.uuid;

-- relation tables

INSERT INTO new_article_fileresources(article_uuid, fileresource_uuid, sortindex)
SELECT article_uuid, identifiable_uuid, sortindex
FROM article_identifiables;

INSERT INTO new_contentnode_contentnodes(parent_contentnode_uuid, child_contentnode_uuid, sortindex)
SELECT parent_contentnode_uuid, child_contentnode_uuid, sortindex
FROM contentnode_contentnode;

-- INSERT INTO new_contentnode_fileresources(contentnode_uuid, fileresource_uuid, sortindex)
-- SELECT contentnode_uuid, identifiable_uuid, sortindex
-- FROM contentnode_identifiables;

INSERT INTO new_contenttree_contentnodes(contenttree_uuid, contentnode_uuid, sortindex)
SELECT contenttree_uuid, contentnode_uuid, sortindex
FROM contenttree_contentnode;

INSERT INTO new_digitalobject_fileresources(digitalobject_uuid, fileresource_uuid, sortindex)
SELECT digitalobject_uuid, fileresource_uuid, sortindex
FROM digitalobject_fileresources;

INSERT INTO new_webpage_fileresources(webpage_uuid, fileresource_uuid, sortindex)
SELECT webpage_uuid, identifiable_uuid, sortindex
FROM webpage_identifiables;

INSERT INTO new_webpage_webpages(parent_webpage_uuid, child_webpage_uuid, sortindex)
SELECT parent_webpage_uuid, child_webpage_uuid, sortindex
FROM webpage_webpage;

INSERT INTO new_website_webpages(website_uuid, webpage_uuid, sortindex)
SELECT website_uuid, webpage_uuid, sortindex
FROM website_webpage;