package de.digitalcollections.cudami.server.business.impl.service.identifiable.alias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@DisplayName("The slug generator")
class SlugGeneratorTest {

  @Mock private UrlAliasRepository urlAliasRepository;

  private SlugGenerator slugGenerator;

  @BeforeEach
  public void beforeEach() {
    urlAliasRepository = mock(UrlAliasRepository.class);
    slugGenerator = new SlugGenerator(urlAliasRepository);
  }

  @DisplayName("returns null for a null input")
  @Test
  public void returnsNullForNull() {
    assertThat(slugGenerator.generateSlug(null)).isNull();
  }

  @DisplayName("returns an empty slug for an empty input")
  @Test
  public void returnsEmptyForEmpty() {
    assertThat(slugGenerator.generateSlug("")).isEqualTo("");
  }

  @DisplayName("transforms to lowercase")
  @Test
  public void transformToLowercase() {
    assertThat(slugGenerator.generateSlug("ABCDE")).isEqualTo("abcde");
  }

  @DisplayName("transforms umlauts to their standard form")
  @Test
  public void transformUmlautsToStandardForm() {
    assertThat(slugGenerator.generateSlug("äÄ")).isEqualTo("aeae");
  }

  @DisplayName("transforms all but digits, characters and dashes to dashes")
  @Test
  public void transformToDashes() {
    assertThat(slugGenerator.generateSlug("1/2 Kilo")).isEqualTo("1-2-kilo");
  }

  @DisplayName("strips dashes at the beginning and at the end")
  @Test
  public void noInitialAndFinalDashes() {
    assertThat(slugGenerator.generateSlug("(foo bar)")).isEqualTo("foo-bar");
  }

  @DisplayName("avoids repeating dashes")
  @Test
  public void noRepeatingDashes() {
    assertThat(slugGenerator.generateSlug("Paragraph $1")).isEqualTo("paragraph-1");
  }
}
