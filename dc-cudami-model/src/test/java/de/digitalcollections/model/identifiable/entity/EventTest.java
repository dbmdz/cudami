package de.digitalcollections.model.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Event")
class EventTest {

  @DisplayName("can create an instance with the help of its inner class builder")
  @Test
  public void testBuilder() {
    Event event =
        Event.builder()
            .uuid(UUID.randomUUID())
            .description(Locale.GERMAN, "Beispiel-Beschreibung")
            .description(Locale.ENGLISH, "Example Description")
            .label(Locale.GERMAN, "Beispiel Beschriftung")
            .label(Locale.ENGLISH, "Example Label")
            .created("2021-01-13T12:34:54")
            .lastModified("2021-01-14T02:45:24")
            .primaryLocalizedUrlAlias(Locale.GERMAN, "blubb")
            .addName(Locale.ITALY, "Bello Gallico")
            .build();
    assertThat(event).isExactlyInstanceOf(Event.class);
  }
}
