package de.digitalcollections.model.jackson.text;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Text;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class StructuredContentTest extends BaseJsonSerializationTest {

  private StructuredContent createObject() {
    StructuredContent structuredContent = new StructuredContent();
    List<ContentBlock> contentBlocks = new ArrayList<>();
    contentBlocks.add(new Text("Test"));
    structuredContent.setContentBlocks(contentBlocks);
    return structuredContent;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    StructuredContent structuredContent = createObject();
    checkSerializeDeserialize(
        structuredContent, "serializedTestObjects/text/StructuredContent.json");
  }
}
