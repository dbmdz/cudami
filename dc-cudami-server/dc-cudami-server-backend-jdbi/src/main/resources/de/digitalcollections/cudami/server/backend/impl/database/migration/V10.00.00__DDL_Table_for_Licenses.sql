CREATE TABLE IF NOT EXISTS licenses (
  uuid UUID NOT NULL PRIMARY KEY,
  acronym VARCHAR COLLATE "ucs_basic",
  label JSONB,
  url VARCHAR COLLATE "ucs_basic" NOT NULL UNIQUE,

  created TIMESTAMP NOT NULL,
  last_modified TIMESTAMP NOT NULL
);