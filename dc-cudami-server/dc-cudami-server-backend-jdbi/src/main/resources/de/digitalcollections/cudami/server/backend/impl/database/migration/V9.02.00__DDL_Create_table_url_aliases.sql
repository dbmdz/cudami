CREATE TABLE IF NOT EXISTS url_aliases (
  uuid UUID PRIMARY KEY,
  website_uuid UUID,
  slug VARCHAR(256) NOT NULL,
  "primary" BOOLEAN NOT NULL,
  last_published TIMESTAMP DEFAULT NULL,
  target_identifiable_type VARCHAR(20) NOT NULL,
  target_entity_type VARCHAR(20),
  target_uuid UUID NOT NULL,
  target_language VARCHAR(2) DEFAULT NULL,
  created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_domain FOREIGN KEY (website_uuid) REFERENCES websites(uuid),
  UNIQUE (website_uuid, slug, target_language)
);
