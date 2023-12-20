package de.digitalcollections.model.jackson.text.contentblock;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.contentblock.IFrame;
import org.junit.jupiter.api.Test;

public class IFrameTest extends BaseJsonSerializationTest {

  private IFrame createObject() {
    IFrame iframe =
        new IFrame(
            "https://www.example.org/index.php?module=CoreAdminHome&amp;action=optOut&amp;language=de",
            "98%",
            "auto",
            "OptOut page");
    return iframe;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    IFrame iframe = createObject();
    checkSerializeDeserialize(iframe, "serializedTestObjects/text/contentblock/IFrame.json");
  }
}
