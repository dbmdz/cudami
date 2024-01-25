package de.digitalcollections.model.jackson.identifiable;

import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import org.junit.jupiter.api.Test;

public class IdentifierTest extends BaseJsonSerializationTest {

  private Identifier createObject() {
    Identifier identifier = new Identifier();
    identifier.setId("bsb10001234");
    identifier.setNamespace("digId");
    return identifier;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Identifier identifier = createObject();
    checkSerializeDeserialize(identifier, "serializedTestObjects/identifiable/Identifier.json");
  }
}
