package de.digitalcollections.cudami.server.backend.impl.asserts;

import de.digitalcollections.model.identifiable.entity.Entity;
import org.assertj.core.api.AbstractAssert;

public class EntityAssert extends AbstractAssert<EntityAssert, Entity> {

  private Entity actual;

  public EntityAssert(Entity entity) {
    super(entity, EntityAssert.class);
    actual = entity;
  }

  @Override
  public EntityAssert isEqualTo(Object expected) {
    if (expected instanceof Entity) {
      ((Entity) expected).setLastModified(actual.getLastModified());
      ((Entity) expected).setCreated(actual.getCreated());
      ((Entity) expected).setRefId(actual.getRefId());
    }

    return super.isEqualTo(expected);
  }
}
