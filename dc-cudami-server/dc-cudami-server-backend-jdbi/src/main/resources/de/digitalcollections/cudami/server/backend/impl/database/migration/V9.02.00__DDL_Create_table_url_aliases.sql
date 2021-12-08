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

/* Sadly in an unique constraint null values are not considered equal. Nulls are always different.
 * Therefore it is possible to have multiple identical slug target_language tuples that have null in the website_uuid coloumn.
 * To overcome this drawback an unique index is necessary that only takes account of rows where the website_uuid is null.
*/
CREATE UNIQUE INDEX IF NOT EXISTS url_aliases_unique_website_uuid_is_null
  ON url_aliases (slug, target_language)
  WHERE website_uuid IS NULL;
