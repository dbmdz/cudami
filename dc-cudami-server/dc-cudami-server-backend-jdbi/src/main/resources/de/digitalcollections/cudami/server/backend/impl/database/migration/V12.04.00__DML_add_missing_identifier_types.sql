/* select the namespaces of existing identifiers */
WITH existing_identifier_namespaces AS (
  SELECT DISTINCT namespace, namespace FROM identifiers
)
INSERT INTO identifiertypes (label, namespace, pattern, uuid)
SELECT
  existing_identifier_namespaces.*,
  /* this allows everything as id */
  '.+',
  /*
   * nasty hack to generate a random UUID without loading an extension
   * (see: https://stackoverflow.com/questions/12505158/generating-a-uuid-in-postgres-for-insert-statement/21327318#21327318)
   * there is a native function "gen_random_uuid" in PostgreSQL >= 13, but we need to support version 12
   * (see: https://www.postgresql.org/docs/current/functions-uuid.html)
   */
  uuid_in(md5(random()::text || random()::text)::cstring)
FROM existing_identifier_namespaces
/* do nothing, if the namespace is already defined */
ON CONFLICT (namespace) DO NOTHING;
