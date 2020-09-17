ALTER TABLE identifiers
  ADD CONSTRAINT unique_namespace_identifier UNIQUE(namespace, identifier);