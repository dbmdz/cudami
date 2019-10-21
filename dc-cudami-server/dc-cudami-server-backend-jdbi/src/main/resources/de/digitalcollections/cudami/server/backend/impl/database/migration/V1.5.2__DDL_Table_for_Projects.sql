CREATE TABLE IF NOT EXISTS projects (
  id SERIAL PRIMARY KEY NOT NULL,
  uuid UUID NOT NULL UNIQUE,

  text JSONB,
  start_date date,
  end_date date
) INHERITS (entities);
