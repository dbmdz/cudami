package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.EntityType;

/** Builder to programatically create a DigitalObject */
public class DigitalObjectBuilder extends EntityBuilder<DigitalObject, DigitalObjectBuilder> {

  @Override
  protected DigitalObject createEntity() {
    return new DigitalObject();
  }

  @Override
  protected EntityType getEntityType() {
    return EntityType.DIGITAL_OBJECT;
  }
}
