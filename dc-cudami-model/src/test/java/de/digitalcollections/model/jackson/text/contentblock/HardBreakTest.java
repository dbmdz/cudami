package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.HardBreak;
import org.junit.jupiter.api.Test;

public class HardBreakTest extends BaseJsonSerializationTest {

  private HardBreak createObject() {
    HardBreak hardBreak = new HardBreak();
    return hardBreak;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    HardBreak hardBreak = createObject();
    checkSerializeDeserialize(hardBreak, "serializedTestObjects/text/contentblock/HardBreak.json");
  }
}
