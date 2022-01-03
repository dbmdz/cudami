CREATE TABLE IF NOT EXISTS headwordentries (
  uuid UUID PRIMARY KEY NOT NULL,
  headword UUID NOT NULL
) INHERITS (articles);