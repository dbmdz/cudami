package de.digitalcollections.model.jackson.semantic;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.semantic.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Tag")
public class TagTest extends BaseJsonSerializationTest {

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    Tag tag = Tag.builder().value("tag-value").build();

    checkSerializeDeserialize(tag, "serializedTestObjects/semantic/Tag.json");
  }
}
