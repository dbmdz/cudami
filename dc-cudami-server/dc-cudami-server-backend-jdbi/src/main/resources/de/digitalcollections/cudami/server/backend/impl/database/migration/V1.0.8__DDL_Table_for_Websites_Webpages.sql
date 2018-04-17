CREATE TABLE IF NOT EXISTS website_webpage (
  website_uuid UUID NOT NULL,
  webpage_uuid UUID NOT NULL UNIQUE,

  PRIMARY KEY (website_uuid, webpage_uuid),
  FOREIGN KEY (website_uuid) REFERENCES websites(uuid),
  FOREIGN KEY (webpage_uuid) REFERENCES webpages(uuid)
);
