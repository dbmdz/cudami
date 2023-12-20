package de.digitalcollections.model.jackson.identifiable.entity.work;

import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The ExpressionType")
class ExpressionTypeTest extends BaseJsonSerializationTest {

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    ExpressionType expressionType =
        ExpressionType.builder().mainType("TEXT").subType("PRINT").build();

    checkSerializeDeserialize(
        expressionType,
        "serializedTestObjects/identifiable/entity/manifestation/ExpressionType.json");
  }
}
