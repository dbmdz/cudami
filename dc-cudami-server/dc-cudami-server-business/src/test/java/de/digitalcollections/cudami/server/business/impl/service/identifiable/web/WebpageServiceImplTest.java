package de.digitalcollections.cudami.server.business.impl.service.identifiable.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The webservice implementation")
class WebpageServiceImplTest {

  WebpageServiceImpl service;

  WebpageRepository repo;

  private static final Locale FALLBACK_LOCALE = Locale.ENGLISH;

  @BeforeEach
  public void beforeEach() {
    repo = mock(WebpageRepository.class);
    service = new WebpageServiceImpl(repo);
  }

  @Test
  @DisplayName("shall not do anything on a node without any label")
  public void ignoreNodeWithNoLabels() {
    Node node = new Node();
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node);
    assertThat(node.getLabel()).isNull();
  }

  @Test
  @DisplayName("shall not do anything on a node without any localized label")
  public void ignoreNodeWithNoLocalizedLabels() {
    Node node = new Node();
    LocalizedText emptyLabel = new LocalizedText();

    node.setLabel(emptyLabel);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node);
    assertThat(node.getLabel()).isEqualTo(emptyLabel);
  }

  @Test
  @DisplayName("shall use the fallback locale for a localized label, if possible")
  public void useFallbackLocale() {
    Node node = new Node();
    LocalizedText label = new LocalizedText();
    label.setText(FALLBACK_LOCALE, "Test");

    node.setLabel(label);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node);
    assertThat(node.getLabel()).isEqualTo(label);
  }

  @Test
  @DisplayName(
      "shall use the first locale (alphabetically) as fallback for a localized label, if possible")
  public void useFirstLocaleAsFallback() {
    LocalizedText expectedFirstLocaleLabel = new LocalizedText(Locale.FRENCH, "faux");

    Node node = new Node();
    LocalizedText label = new LocalizedText();
    label.setText(Locale.ITALIAN, "vero");
    label.setText(Locale.FRENCH, "faux");

    node.setLabel(label);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node);
    assertThat(node.getLabel()).isEqualTo(expectedFirstLocaleLabel);
  }

  @Test
  @DisplayName("shall return a label in the desired locale, if possible")
  public void useDesiredLocale() {
    LocalizedText expectedGermanLabel = new LocalizedText(Locale.GERMAN, "richtig");

    Node node = new Node();
    LocalizedText label = new LocalizedText();
    label.setText(FALLBACK_LOCALE, "falsch");
    label.setText(Locale.GERMAN, "richtig");

    node.setLabel(label);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node);
    assertThat(node.getLabel()).isEqualTo(expectedGermanLabel);
  }
}
