package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.Heading;
import de.digitalcollections.model.text.contentblock.Text;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class HeadingTest extends BaseJsonSerializationTest {

  private Heading createObject() {
    Heading heading = new Heading();
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("level", 3);
    heading.setAttributes(attributes);
    Text text = new Text("Imprint");
    heading.addContentBlock(text);
    return heading;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Heading heading = createObject();
    checkSerializeDeserialize(heading, "serializedTestObjects/text/contentblock/Heading.json");
  }
}
