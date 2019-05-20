CREATE TABLE IF NOT EXISTS identifiers (
  identifiable_uuid UUID NOT NULL,

  namespace VARCHAR NOT NULL,
  identifier VARCHAR NOT NULL,

  PRIMARY KEY (identifiable_uuid, namespace, identifier)
);
