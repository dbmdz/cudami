package de.digitalcollections.cudami.model.api.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;

/**
 * An entity.
 */
public interface Entity extends Identifiable {

  EntityType getEntityType();

  void setEntityType(EntityType entityType);
}
