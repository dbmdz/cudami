CREATE TABLE IF NOT EXISTS items (
  language VARCHAR,
  publication_date VARCHAR,
  publication_place VARCHAR,
  publisher VARCHAR,
  version VARCHAR
) INHERITS (entities);
