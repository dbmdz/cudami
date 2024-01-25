package de.digitalcollections.model.identifiable.entity.geo.location;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Human Settlement")
class HumanSettlementTest {

  protected static final Locale LOCALE_UND_LATN =
      new Locale.Builder().setLanguage("und").setScript("Latn").build();

  @DisplayName("has the same hash code for two identical (by name) human settlements")
  @Test
  void hashCodeForIdenticalName() {
    HumanSettlement humanSettlement1 =
        HumanSettlement.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();
    HumanSettlement humanSettlement2 =
        HumanSettlement.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();

    assertThat(humanSettlement1.hashCode()).isEqualTo(humanSettlement2.hashCode());
  }

  @DisplayName("has a different hash code for two non identical (by name) human settlements")
  @Test
  void hashCodeForDifferentName() {
    HumanSettlement humanSettlement1 =
        HumanSettlement.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();
    HumanSettlement humanSettlement2 =
        HumanSettlement.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test2")).build();

    assertThat(humanSettlement1.hashCode()).isNotEqualTo(humanSettlement2.hashCode());
  }
}
