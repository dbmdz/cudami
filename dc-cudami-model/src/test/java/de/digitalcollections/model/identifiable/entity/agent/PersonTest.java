package de.digitalcollections.model.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Person")
class PersonTest {

  protected static final Locale LOCALE_UND_LATN =
      new Locale.Builder().setLanguage("und").setScript("Latn").build();

  @DisplayName("has the same hash code for two identical (by name) persons")
  @Test
  void hashCodeForIdenticalName() {
    Person person1 = Person.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();
    Person person2 = Person.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();

    assertThat(person1.hashCode()).isEqualTo(person2.hashCode());
  }

  @DisplayName("has a different hash code for two non identical (by name) persons")
  @Test
  void hashCodeForDifferentName() {
    Person person1 = Person.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();
    Person person2 = Person.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test2")).build();

    assertThat(person1.hashCode()).isNotEqualTo(person2.hashCode());
  }
}
