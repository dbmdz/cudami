package de.digitalcollections.model.jackson.view;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.view.RenderingHints;
import org.junit.jupiter.api.Test;

public class RenderingHintsTest extends BaseJsonSerializationTest {

  private RenderingHints createObject() {
    RenderingHints hints = new RenderingHints();
    hints.setShowInPageNavigation(true);
    hints.setTemplateName("my-template");
    return hints;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    RenderingHints hints = createObject();
    checkSerializeDeserialize(hints, "serializedTestObjects/view/RenderingHints.json");
  }
}
