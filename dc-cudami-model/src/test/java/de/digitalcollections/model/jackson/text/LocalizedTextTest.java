package de.digitalcollections.model.jackson.text;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class LocalizedTextTest extends BaseJsonSerializationTest {

  private LocalizedText createObject() {
    return new LocalizedText(Locale.ITALY, "Buon Giorno!");
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    LocalizedText localizedText = createObject();
    checkSerializeDeserialize(localizedText, "serializedTestObjects/text/LocalizedText.json");
  }
}
