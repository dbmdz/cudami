CREATE TABLE IF NOT EXISTS new_identifiables (
  uuid UUID PRIMARY KEY NOT NULL,

  created TIMESTAMP NOT NULL,
  description JSONB,
  identifiable_type VARCHAR NOT NULL,
  label JSONB,
  last_modified TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS new_entities (
  entity_type VARCHAR NOT NULL
) INHERITS (new_identifiables);

-- website - webpages - relations

CREATE TABLE IF NOT EXISTS new_websites (
  uuid UUID PRIMARY KEY NOT NULL,
  url VARCHAR NOT NULL UNIQUE,
  registration_date DATE
) INHERITS (new_entities);

CREATE TABLE IF NOT EXISTS new_webpages (
  uuid UUID PRIMARY KEY NOT NULL,
  text JSONB
) INHERITS (new_identifiables);

CREATE TABLE IF NOT EXISTS new_website_webpages (
  website_uuid UUID NOT NULL,
  webpage_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (website_uuid, webpage_uuid),
  FOREIGN KEY (website_uuid) REFERENCES new_websites(uuid),
  FOREIGN KEY (webpage_uuid) REFERENCES new_webpages(uuid)
);

CREATE TABLE IF NOT EXISTS new_webpage_webpages (
  parent_webpage_uuid UUID NOT NULL,
  child_webpage_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (parent_webpage_uuid, child_webpage_uuid),
  FOREIGN KEY (parent_webpage_uuid) REFERENCES new_webpages(uuid),
  FOREIGN KEY (child_webpage_uuid) REFERENCES new_webpages(uuid)
);

-- contenttree - contentnodes - relations

CREATE TABLE IF NOT EXISTS new_contenttrees (
  uuid UUID PRIMARY KEY NOT NULL
) INHERITS (new_entities);

CREATE TABLE IF NOT EXISTS new_contentnodes (
  uuid UUID PRIMARY KEY NOT NULL
) INHERITS (new_identifiables);

CREATE TABLE IF NOT EXISTS new_contenttree_contentnodes (
  contenttree_uuid UUID NOT NULL,
  contentnode_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (contenttree_uuid, contentnode_uuid),
  FOREIGN KEY (contenttree_uuid) REFERENCES new_contenttrees(uuid),
  FOREIGN KEY (contentnode_uuid) REFERENCES new_contentnodes(uuid)
);

CREATE TABLE IF NOT EXISTS new_contentnode_contentnodes (
  parent_contentnode_uuid UUID NOT NULL,
  child_contentnode_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (parent_contentnode_uuid, child_contentnode_uuid),
  FOREIGN KEY (parent_contentnode_uuid) REFERENCES new_contentnodes(uuid),
  FOREIGN KEY (child_contentnode_uuid) REFERENCES new_contentnodes(uuid)
);

-- fileresources



-- article - fileresources - relations

CREATE TABLE IF NOT EXISTS new_articles (
  uuid UUID PRIMARY KEY NOT NULL,
  text JSONB
) INHERITS (new_entities);

CREATE TABLE IF NOT EXISTS new_article_fileresources (
  article_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (article_uuid, fileresource_uuid),
  FOREIGN KEY (article_uuid) REFERENCES new_articles(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES new_fileresources(uuid)
);


CREATE TABLE IF NOT EXISTS new_digitalobjects (
  uuid UUID PRIMARY KEY NOT NULL,
) INHERITS (new_identifiables);

CREATE TABLE IF NOT EXISTS new_fileresources (
  uuid UUID PRIMARY KEY NOT NULL,
  filename VARCHAR NOT NULL,
  mimetype VARCHAR NOT NULL,
  size_in_bytes BIGINT NOT NULL,
  uri VARCHAR(512)
) INHERITS (new_identifiables);

CREATE TABLE IF NOT EXISTS new_fileresources_audio (
  uuid UUID PRIMARY KEY NOT NULL,
  duration int -- (in seconds)
) INHERITS (new_fileresources);

CREATE TABLE IF NOT EXISTS new_fileresources_image (
  uuid UUID PRIMARY KEY NOT NULL,
  height int, -- (in pixel)
  width  int -- (in pixel)
) INHERITS (new_fileresources);

CREATE TABLE IF NOT EXISTS new_fileresources_video (
  uuid UUID PRIMARY KEY NOT NULL,
  duration int -- (in seconds)
) INHERITS (new_fileresources);



CREATE TABLE IF NOT EXISTS new_digitalobject_fileresources (
  digitalobject_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (digitalobject_uuid, fileresource_uuid),
  FOREIGN KEY (digitalobject_uuid) REFERENCES new_digitalobjects(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES new_fileresources(uuid)
);

CREATE TABLE IF NOT EXISTS new_contentnode_fileresources (
  contentnode_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (contentnode_uuid, fileresource_uuid),
  FOREIGN KEY (contentnode_uuid) REFERENCES new_contentnodes(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES new_fileresources(uuid)
);

CREATE TABLE IF NOT EXISTS new_webpage_fileresources (
  webpage_uuid UUID NOT NULL,
  fileresource_uuid UUID NOT NULL UNIQUE,
  sortIndex SMALLINT,

  PRIMARY KEY (webpage_uuid, fileresource_uuid),
  FOREIGN KEY (webpage_uuid) REFERENCES new_webpages(uuid),
  FOREIGN KEY (fileresource_uuid) REFERENCES new_fileresources(uuid)
);
