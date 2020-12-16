CREATE TABLE IF NOT EXISTS rendering_templates (
  uuid UUID NOT NULL PRIMARY KEY UNIQUE,

  description JSONB,
  label JSONB,
  name VARCHAR NOT NULL
);
