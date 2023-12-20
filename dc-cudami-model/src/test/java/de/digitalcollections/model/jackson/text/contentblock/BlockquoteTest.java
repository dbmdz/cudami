package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.Blockquote;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import org.junit.jupiter.api.Test;

public class BlockquoteTest extends BaseJsonSerializationTest {

  private Blockquote createObject() {
    Blockquote blockquote = new Blockquote();
    Paragraph paragraph = new Paragraph();
    Text content = new Text("Das ist ein Test");
    paragraph.addContentBlock(content);
    blockquote.addContentBlock(content);
    return blockquote;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Blockquote blockquote = createObject();
    checkSerializeDeserialize(
        blockquote, "serializedTestObjects/text/contentblock/Blockquote.json");
  }
}
