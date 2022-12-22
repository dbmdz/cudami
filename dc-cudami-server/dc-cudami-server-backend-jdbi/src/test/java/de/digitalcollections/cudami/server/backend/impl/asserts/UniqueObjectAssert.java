package de.digitalcollections.cudami.server.backend.impl.asserts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import org.assertj.core.api.AbstractAssert;

/**
 * The IdentifiableAssert compares two identifiables field by field with the exception of
 * lastModified.
 */
public class UniqueObjectAssert extends AbstractAssert<UniqueObjectAssert, UniqueObject> {

  private UniqueObject actual;

  public UniqueObjectAssert(UniqueObject uniqueObject) {
    super(uniqueObject, UniqueObjectAssert.class);
    actual = uniqueObject;
  }

  @Override
  public UniqueObjectAssert isEqualTo(Object expected) {
    if (expected instanceof UniqueObject) {
      ((UniqueObject) expected).setLastModified(actual.getLastModified());
      ((UniqueObject) expected).setCreated(actual.getCreated());
    }

    return super.isEqualTo(expected);
  }

  public UniqueObjectAssert isEqualToComparingFieldByField(Identifiable expected) {
    ObjectMapper objectMapper = new DigitalCollectionsObjectMapper();
    String serializedActual = null;
    String serializedExpected = null;
    try {
      serializedActual = objectMapper.writeValueAsString(actual);
      serializedExpected = objectMapper.writeValueAsString(expected);
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
