package de.digitalcollections.model.jackson.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.semantic.Headword;
import java.net.MalformedURLException;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class HeadwordEntryTest extends BaseJsonSerializationTest {

  private HeadwordEntry createObject() throws MalformedURLException {
    HeadwordEntry headwordEntry = new HeadwordEntry(new Headword("Kaiserschmarrn", Locale.GERMAN));
    return headwordEntry;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    HeadwordEntry headwordEntry = createObject();
    checkSerializeDeserialize(
        headwordEntry, "serializedTestObjects/identifiable/entity/HeadwordEntry.json");
  }
}
