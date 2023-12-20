package de.digitalcollections.model.jackson.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The entity serialization")
public class EntityTest extends BaseJsonSerializationTest {

  private Entity createObject() {
    Entity entity = new Entity();
    entity.setLabel(new LocalizedText(Locale.GERMAN, "Bayerische Staatsbibliothek"));
    entity.setPreviewImage(new ImageFileResource());
    return entity;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Entity entity = createObject();
    checkSerializeDeserialize(entity, "serializedTestObjects/identifiable/entity/Entity.json");
  }
}
