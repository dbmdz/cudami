package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.CodeBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import org.junit.jupiter.api.Test;

public class CodeBlockTest extends BaseJsonSerializationTest {

  private CodeBlock createObject() {
    CodeBlock codeBlock = new CodeBlock();
    Paragraph paragraph = new Paragraph();
    Text content = new Text("Das ist ein Test");
    paragraph.addContentBlock(content);
    codeBlock.addContentBlock(content);
    return codeBlock;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    CodeBlock codeBlock = createObject();
    checkSerializeDeserialize(codeBlock, "serializedTestObjects/text/contentblock/CodeBlock.json");
  }
}
