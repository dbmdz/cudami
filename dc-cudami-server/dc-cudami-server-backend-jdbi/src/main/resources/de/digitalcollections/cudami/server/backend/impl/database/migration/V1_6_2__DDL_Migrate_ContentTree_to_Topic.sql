-- drop constraints
ALTER TABLE IF EXISTS contentnode_contentnodes DROP CONSTRAINT IF EXISTS new_contentnode_contentnodes_parent_contentnode_uuid_fkey;
ALTER TABLE IF EXISTS contentnode_contentnodes DROP CONSTRAINT IF EXISTS new_contentnode_contentnodes_child_contentnode_uuid_fkey;
ALTER TABLE IF EXISTS contentnode_contentnodes DROP CONSTRAINT IF EXISTS new_contentnode_contentnodes_pkey;
ALTER TABLE IF EXISTS contentnode_contentnodes DROP CONSTRAINT IF EXISTS new_contentnode_contentnodes_child_contentnode_uuid_key;
ALTER TABLE IF EXISTS contentnode_entities DROP CONSTRAINT IF EXISTS new_contentnode_entities_contentnode_uuid_fkey;
ALTER TABLE IF EXISTS contentnode_entities DROP CONSTRAINT IF EXISTS new_contentnode_entities_pkey;
ALTER TABLE IF EXISTS contentnode_entities DROP CONSTRAINT IF EXISTS new_contentnode_entities_entity_uuid_key;
ALTER TABLE IF EXISTS contentnode_fileresources DROP CONSTRAINT IF EXISTS new_contentnode_fileresources_contentnode_uuid_fkey;
ALTER TABLE IF EXISTS contentnode_fileresources DROP CONSTRAINT IF EXISTS new_contentnode_fileresources_pkey;
ALTER TABLE IF EXISTS contentnode_fileresources DROP CONSTRAINT IF EXISTS new_contentnode_fileresources_fileresource_uuid_key;

ALTER TABLE IF EXISTS contenttree_contentnodes DROP CONSTRAINT IF EXISTS new_contenttree_contentnodes_contenttree_uuid_fkey;
ALTER TABLE IF EXISTS contenttree_contentnodes DROP CONSTRAINT IF EXISTS new_contenttree_contentnodes_contentnode_uuid_fkey;
ALTER TABLE IF EXISTS contenttree_contentnodes DROP CONSTRAINT IF EXISTS new_contenttree_contentnodes_pkey;
ALTER TABLE IF EXISTS contenttree_contentnodes DROP CONSTRAINT IF EXISTS new_contenttree_contentnodes_contentnode_uuid_key;

ALTER TABLE IF EXISTS contentnodes DROP CONSTRAINT IF EXISTS new_contentnodes_pkey;
ALTER TABLE IF EXISTS contenttrees DROP CONSTRAINT IF EXISTS new_contenttrees_pkey;

-- rename tables
ALTER TABLE IF EXISTS contentnode_contentnodes RENAME TO subtopic_subtopics;
ALTER TABLE IF EXISTS contentnode_entities RENAME TO subtopic_entities;
ALTER TABLE IF EXISTS contentnode_fileresources RENAME TO subtopic_fileresources;
ALTER TABLE IF EXISTS contentnodes RENAME TO subtopics;

ALTER TABLE IF EXISTS contenttree_contentnodes RENAME TO topic_subtopics;
ALTER TABLE IF EXISTS contenttrees RENAME TO topics;

-- rename columns
ALTER TABLE IF EXISTS subtopic_entities RENAME COLUMN contentnode_uuid TO subtopic_uuid;
ALTER TABLE IF EXISTS subtopic_fileresources RENAME COLUMN contentnode_uuid TO subtopic_uuid;
ALTER TABLE IF EXISTS subtopic_subtopics RENAME COLUMN parent_contentnode_uuid TO parent_subtopic_uuid;
ALTER TABLE IF EXISTS subtopic_subtopics RENAME COLUMN child_contentnode_uuid TO child_subtopic_uuid;
ALTER TABLE IF EXISTS topic_subtopics RENAME COLUMN contenttree_uuid TO topic_uuid;
ALTER TABLE IF EXISTS topic_subtopics RENAME COLUMN contentnode_uuid TO subtopic_uuid;

-- add constraints
ALTER TABLE IF EXISTS topics ADD PRIMARY KEY (uuid);
ALTER TABLE IF EXISTS subtopics ADD PRIMARY KEY (uuid);

ALTER TABLE IF EXISTS subtopic_subtopics ADD PRIMARY KEY (parent_subtopic_uuid, child_subtopic_uuid);
ALTER TABLE IF EXISTS subtopic_subtopics ADD FOREIGN KEY (parent_subtopic_uuid) REFERENCES subtopics(uuid);
ALTER TABLE IF EXISTS subtopic_subtopics ADD FOREIGN KEY (child_subtopic_uuid) REFERENCES subtopics(uuid);

ALTER TABLE IF EXISTS subtopic_entities ADD PRIMARY KEY (subtopic_uuid, entity_uuid);
ALTER TABLE IF EXISTS subtopic_entities ADD FOREIGN KEY (subtopic_uuid) REFERENCES subtopics(uuid);
-- entities foreign key does not work on parent tables: caveat of inheritance!!!
-- ALTER TABLE IF EXISTS subtopic_entities ADD FOREIGN KEY (entity_uuid) REFERENCES entities(uuid);

ALTER TABLE IF EXISTS subtopic_fileresources ADD PRIMARY KEY (subtopic_uuid, fileresource_uuid);
ALTER TABLE IF EXISTS subtopic_fileresources ADD FOREIGN KEY (subtopic_uuid) REFERENCES subtopics(uuid);
-- fileresources foreign key does not work on parent tables: caveat of inheritance!!!
-- ALTER TABLE IF EXISTS subtopic_fileresources ADD FOREIGN KEY (fileresource_uuid) REFERENCES fileresources(uuid);

ALTER TABLE IF EXISTS topic_subtopics ADD PRIMARY KEY (topic_uuid, subtopic_uuid);
ALTER TABLE IF EXISTS topic_subtopics ADD FOREIGN KEY (topic_uuid) REFERENCES topics(uuid);
ALTER TABLE IF EXISTS topic_subtopics ADD FOREIGN KEY (subtopic_uuid) REFERENCES subtopics(uuid);

-- migrate "CONTENT_TREE" entity types
UPDATE entities SET entity_type='TOPIC' WHERE entity_type='CONTENT_TREE';