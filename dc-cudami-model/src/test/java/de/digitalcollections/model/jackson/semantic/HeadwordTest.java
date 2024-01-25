package de.digitalcollections.model.jackson.semantic;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.semantic.Headword;
import java.net.MalformedURLException;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class HeadwordTest extends BaseJsonSerializationTest {

  private Headword createObject() throws MalformedURLException {
    Headword headword = new Headword("Kaiserschmarrn", Locale.GERMAN);
    return headword;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Headword headword = createObject();
    checkSerializeDeserialize(headword, "serializedTestObjects/semantic/Headword.json");
  }

  @Test
  public void testLabelNormalizedSerializeDeserialize() throws Exception {
    Headword headword = new Headword("État", Locale.FRENCH);
    headword.setLabelNormalized("Etat");
    checkSerializeDeserialize(headword, "serializedTestObjects/semantic/Headword_normalized.json");
  }
}
