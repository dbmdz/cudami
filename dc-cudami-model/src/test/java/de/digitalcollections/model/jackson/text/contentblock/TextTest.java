package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.Mark;
import de.digitalcollections.model.text.contentblock.Text;
import org.junit.jupiter.api.Test;

public class TextTest extends BaseJsonSerializationTest {

  private Text createObject() {
    Text text = new Text("TEST");
    text.addMark(new Mark("strong"));
    text.addMark(new Mark("em"));
    return text;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Text text = createObject();
    checkSerializeDeserialize(text, "serializedTestObjects/text/contentblock/Text.json");
  }
}
