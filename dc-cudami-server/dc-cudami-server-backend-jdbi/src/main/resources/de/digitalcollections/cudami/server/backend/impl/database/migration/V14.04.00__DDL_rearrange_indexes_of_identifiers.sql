/**
 * Rearrangement of the indexes on table identifiers
 * to support "index only" scans and speed up repo's `getByIdentifier`
 */
DROP INDEX IF EXISTS idx_identifiers_uniq;
ALTER TABLE identifiers
  DROP CONSTRAINT IF EXISTS unique_namespace_identifier_identifiable,
  ADD CONSTRAINT unique_namespace_identifier_identifiable UNIQUE (identifiable, namespace, identifier),
  DROP CONSTRAINT IF EXISTS unique_namespace_identifier,
  ADD CONSTRAINT unique_namespace_identifier UNIQUE (namespace, identifier) INCLUDE (identifiable);

