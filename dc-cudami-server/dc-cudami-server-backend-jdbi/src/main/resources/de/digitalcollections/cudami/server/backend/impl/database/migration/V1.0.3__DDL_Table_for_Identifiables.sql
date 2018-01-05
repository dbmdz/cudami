CREATE TABLE IF NOT EXISTS identifiables (
  id SERIAL PRIMARY KEY NOT NULL,
  uuid UUID NOT NULL UNIQUE,

  created TIMESTAMP NOT NULL,
  description JSONB,
  identifiable_type VARCHAR NOT NULL,
  label JSONB,
  last_modified TIMESTAMP NOT NULL,
  iiif_image JSONB
);
