package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.Mark;
import org.junit.jupiter.api.Test;

public class MarkTest extends BaseJsonSerializationTest {

  private Mark createObject() {
    Mark mark = new Mark();
    mark.setType("link");
    mark.addAttribute("href", "https://www.example.org");
    return mark;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Mark mark = createObject();
    checkSerializeDeserialize(mark, "serializedTestObjects/text/contentblock/Mark.json");
  }
}
