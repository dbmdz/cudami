ALTER TABLE rel_entity_entities
  ADD CONSTRAINT fk_predicate_predicates FOREIGN KEY (predicate) REFERENCES predicates(value);