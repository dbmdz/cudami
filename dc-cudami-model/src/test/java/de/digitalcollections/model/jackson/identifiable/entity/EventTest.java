package de.digitalcollections.model.jackson.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EventTest extends BaseJsonSerializationTest {

  private Event createObject() throws MalformedURLException {
    Event event =
        Event.builder()
            .uuid(UUID.fromString("e17f3225-f907-4194-9c2a-c8612174e0e5"))
            .description(Locale.GERMAN, "Beispiel-Beschreibung")
            .description(Locale.ENGLISH, "Example Description")
            .label(Locale.GERMAN, "Beispiel Beschriftung")
            .label(Locale.ENGLISH, "Example Label")
            .created("2021-01-13T12:34:54")
            .lastModified("2021-01-14T02:45:24")
            .addName(Locale.ITALY, "Bello Gallico")
            .build();
    return event;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Event event = createObject();
    checkSerializeDeserialize(event, "serializedTestObjects/identifiable/entity/Event.json");
  }
}
