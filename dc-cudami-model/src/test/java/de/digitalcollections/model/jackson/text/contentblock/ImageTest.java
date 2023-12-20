package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.Image;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ImageTest extends BaseJsonSerializationTest {

  private Image createObject() {
    Image image = new Image();
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("alignment", "left");
    attributes.put("altText", "This is the alt-text.");
    attributes.put("caption", "This is the caption.");
    attributes.put("linkNewTab", true);
    attributes.put("linkUrl", "https://external.content.org");
    attributes.put("resourceId", "135ec10b-ac65-4217-83fc-db5e9ff62cac");
    attributes.put("title", "This is the title.");
    attributes.put("url", "https://www.bsb-muenchen.de/logo.png");
    attributes.put("width", "33%");
    image.setAttributes(attributes);
    return image;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Image image = createObject();
    checkSerializeDeserialize(image, "serializedTestObjects/text/contentblock/Image.json");
  }
}
