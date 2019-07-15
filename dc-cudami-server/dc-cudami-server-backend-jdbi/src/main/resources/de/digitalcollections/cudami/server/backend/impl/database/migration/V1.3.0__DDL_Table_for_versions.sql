CREATE TABLE IF NOT EXISTS versions (
  uuid UUID primary key,
  version_value integer,
  description varchar,
  created date NOT NULL,
  type_key varchar,
  instance_key varchar,
  instance_version_key varchar,
  status varchar(16)
);

create unique index idx_versions_instance_version_key on versions(instance_version_key);
create index idx_versions_instance_key on versions(instance_key);
