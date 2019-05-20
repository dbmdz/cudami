CREATE TABLE IF NOT EXISTS identifiers (
  uuid UUID primary key,

  identifiable UUID NOT NULL,
  namespace VARCHAR NOT NULL,
  identifier VARCHAR NOT NULL
);

create unique index idx_identifiers_uniq on identifiers(identifiable, namespace, identifier);
