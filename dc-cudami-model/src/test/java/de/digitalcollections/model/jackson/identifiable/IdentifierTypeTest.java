package de.digitalcollections.model.jackson.identifiable;

import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class IdentifierTypeTest extends BaseJsonSerializationTest {

  private IdentifierType createObject() {
    return IdentifierType.builder()
        .uuid(UUID.fromString("61033a4d-318f-4aa4-96b1-6663137bb807"))
        .pattern("^(\\w{3})(\\d{4})(\\d{4})$")
        .label("Digital object id")
        .namespace("digId")
        .build();
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    IdentifierType identifierType = createObject();
    checkSerializeDeserialize(
        identifierType, "serializedTestObjects/identifiable/IdentifierType.json");
  }
}
