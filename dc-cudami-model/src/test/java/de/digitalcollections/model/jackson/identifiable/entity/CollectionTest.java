package de.digitalcollections.model.jackson.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.time.LocalDate;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class CollectionTest extends BaseJsonSerializationTest {

  private Collection createObject() {
    Collection collection = new Collection();
    LocalizedStructuredContent localizedStructuredContent = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    ContentBlock contentBlock = new Paragraph("Collection of all medieval manuscripts");
    structuredContent.addContentBlock(contentBlock);
    localizedStructuredContent.put(Locale.ENGLISH, structuredContent);
    collection.setDescription(localizedStructuredContent);
    collection.setPublicationStart(LocalDate.MIN);
    collection.setPublicationEnd(LocalDate.MAX);
    return collection;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Collection collection = createObject();
    assertThat(collection.getChildren()).isNotNull().isEmpty();
    assertThat(collection.getEntities()).isNotNull().isEmpty();
    Collection deserializedCollection =
        checkSerializeDeserialize(
            collection, "serializedTestObjects/identifiable/entity/Collection.json");
    assertThat(deserializedCollection.getChildren()).isNotNull().isEmpty();
    assertThat(deserializedCollection.getEntities()).isNotNull().isEmpty();
  }

  @Test
  public void testBuilder() {
    Collection collection =
        Collection.builder().label(Locale.forLanguageTag("en-Latn"), "A Collection").build();
    assertThat(collection.getChildren()).isNotNull();
    assertThat(collection.getEntities()).isNotNull();
    assertThat(collection.getCreated()).isNull();
  }
}
