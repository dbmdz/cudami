package de.digitalcollections.model.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.text.LocalizedText;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The CorporateBody")
class CorporateBodyTest {

  protected static final Locale LOCALE_UND_LATN =
      new Locale.Builder().setLanguage("und").setScript("Latn").build();

  @DisplayName("can create an instance with the help of its inner class builder")
  @Test
  public void testBuilder() throws MalformedURLException {
    CorporateBody corporateBody =
        CorporateBody.builder()
            .uuid(UUID.randomUUID())
            .description(Locale.GERMAN, "Beispiel-Bild")
            .description(Locale.ENGLISH, "Example Image")
            .label(Locale.GERMAN, "Beispielbild")
            .label(Locale.ENGLISH, "Example Image")
            .created("2021-01-13T12:34:54")
            .lastModified("2021-01-14T02:45:24")
            .primaryLocalizedUrlAlias(Locale.GERMAN, "blubb")
            .homepageUrl(new URL("http://foo.bar"))
            .text(Locale.GERMAN, "foo")
            .build();
    assertThat(corporateBody).isExactlyInstanceOf(CorporateBody.class);
  }

  @DisplayName("has the same hash code for two identical (by name) corporate bodies")
  @Test
  void hashCodeForIdenticalName() {
    CorporateBody corporateBody1 =
        CorporateBody.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();
    CorporateBody corporateBody2 =
        CorporateBody.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();

    assertThat(corporateBody1.hashCode()).isEqualTo(corporateBody2.hashCode());
  }

  @DisplayName("has a different hash code for two non identical (by name) corporate bodies")
  @Test
  void hashCodeForDifferentName() {
    CorporateBody corporateBody1 =
        CorporateBody.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test")).build();
    CorporateBody corporateBody2 =
        CorporateBody.builder().name(new LocalizedText(LOCALE_UND_LATN, "Test2")).build();

    assertThat(corporateBody1.hashCode()).isNotEqualTo(corporateBody2.hashCode());
  }
}
