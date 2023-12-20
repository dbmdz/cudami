package de.digitalcollections.model.jackson.identifiable.entity.work;

import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ItemTest extends BaseJsonSerializationTest {

  private Item createObject() {
    Item item = new Item();
    item.setLabel(
        new LocalizedText(
            Locale.GERMAN,
            "Zimmer-Gymnastik ohne Geräte : 50 tägliche Übungen für die gesamte Körpermuskulatur, zur Erhaltung der Gesundheit und Förderung der Gewandtheit"));
    final ImageFileResource previewImage = new ImageFileResource();
    previewImage.setUuid(UUID.fromString("6bed2ff9-4ad5-4e18-b520-bb9843fe9a73"));
    item.setPreviewImage(previewImage);
    item.setIdentifiers(Set.of(Identifier.builder().namespace("namespace").id("id").build()));
    return item;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Item item = createObject();
    checkSerializeDeserialize(item, "serializedTestObjects/identifiable/entity/item/Item.json");
  }
}
