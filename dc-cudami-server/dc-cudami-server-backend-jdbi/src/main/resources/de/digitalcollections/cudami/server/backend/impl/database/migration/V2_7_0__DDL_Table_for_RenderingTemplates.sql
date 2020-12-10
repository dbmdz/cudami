CREATE TABLE IF NOT EXISTS rendering_templates (
  id SERIAL PRIMARY KEY NOT NULL,
  uuid UUID NOT NULL UNIQUE,

  description JSONB,
  label JSONB,
  name VARCHAR NOT NULL
);
