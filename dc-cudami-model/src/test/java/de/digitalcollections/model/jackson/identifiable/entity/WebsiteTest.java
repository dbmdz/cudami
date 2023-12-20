package de.digitalcollections.model.jackson.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class WebsiteTest extends BaseJsonSerializationTest {

  private Website createObject() throws MalformedURLException {
    Website website = new Website(new URL("http://www.example.org/"));
    LocalizedStructuredContent localizedStructuredContent = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    ContentBlock contentBlock = new Paragraph("Buon Giorno!");
    structuredContent.addContentBlock(contentBlock);
    localizedStructuredContent.put(Locale.ITALY, structuredContent);
    website.setDescription(localizedStructuredContent);
    return website;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Website website = createObject();
    checkSerializeDeserialize(website, "serializedTestObjects/identifiable/entity/Website.json");
  }
}
