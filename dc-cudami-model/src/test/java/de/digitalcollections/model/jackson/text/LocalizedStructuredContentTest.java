package de.digitalcollections.model.jackson.text;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class LocalizedStructuredContentTest extends BaseJsonSerializationTest {
  private LocalizedStructuredContent createObject() {
    LocalizedStructuredContent localizedStructuredContent = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    ContentBlock contentBlock = new Paragraph("Buon Giorno!");
    structuredContent.addContentBlock(contentBlock);
    localizedStructuredContent.put(Locale.ITALY, structuredContent);
    return localizedStructuredContent;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    LocalizedStructuredContent localizedStructuredContent = createObject();
    checkSerializeDeserialize(
        localizedStructuredContent, "serializedTestObjects/text/LocalizedStructuredContent.json");
  }
}
