package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.HardBreak;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import org.junit.jupiter.api.Test;

public class ParagraphTest extends BaseJsonSerializationTest {

  private Paragraph createObject() {
    Paragraph paragraph = new Paragraph();
    Text text1 = new Text("Imprint");
    paragraph.addContentBlock(text1);
    paragraph.addContentBlock(new HardBreak());
    Text text2 = new Text("Privacy");
    paragraph.addContentBlock(text2);
    return paragraph;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Paragraph paragraph = createObject();
    checkSerializeDeserialize(paragraph, "serializedTestObjects/text/contentblock/Paragraph.json");
  }
}
