package de.digitalcollections.cudami.server.backend.impl.asserts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
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

  public EntityAssert isEqualToComparingFieldByField(DigitalObject expected) {
    ObjectMapper objectMapper = new DigitalCollectionsObjectMapper();
    String serializedActual = null;
    String serializedExpected = null;
    try {
      serializedActual = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual);
      serializedExpected =
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected);
      this.objects.assertEqual(this.info, serializedActual, serializedExpected);
      return this.myself;
    } catch (JsonProcessingException e) {
      return CudamiAssertions.fail(
          "Actual ("
              + serializedActual
              + ") cannot be compared to expected ("
              + serializedExpected
              + ") :"
              + e,
          e);
    }
  }
}
