package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Identifiable-UrlAlias-Align-Helper tests")
public class IdentifiableUrlAliasAlignHelperTest {

  CudamiConfig cudamiConfig;
  IdentifiableUrlAliasAlignHelper.SlugGeneratorService slugGeneratorService;

  @BeforeEach
  public void setup() {
    cudamiConfig = new CudamiConfig();
    CudamiConfig.UrlAlias urlAliasConfig = new CudamiConfig.UrlAlias();
    urlAliasConfig.setGenerationExcludes(List.of(EntityType.DIGITAL_OBJECT));
    cudamiConfig.setUrlAlias(urlAliasConfig);

    slugGeneratorService = mock(IdentifiableUrlAliasAlignHelper.SlugGeneratorService.class);
  }

  @DisplayName("does not guarantee URLAliases for some entity types")
  @Test
  public void noUrlAliasesForSomeEntityTypes() throws CudamiServiceException {
    DigitalObject identifiable = new DigitalObject();
    identifiable.setLabel("label");
    IdentifiableUrlAliasAlignHelper.checkDefaultAliases(
        identifiable, cudamiConfig, slugGeneratorService);
    assertThat(identifiable.getLocalizedUrlAliases()).isNull();
  }

  @DisplayName("can create an LocalizedUrlAlias, when it's missing")
  @Test
  public void createLocalizedUrlAliasWhenMissing() throws CudamiServiceException {
    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");
    when(slugGeneratorService.apply(any(), any(), any())).thenReturn("label");

    IdentifiableUrlAliasAlignHelper.checkDefaultAliases(
        identifiable, cudamiConfig, slugGeneratorService);
    assertThat(identifiable.getLocalizedUrlAliases()).isNotNull();
  }

  @DisplayName("sets all required attribute of a created default UrlAlias")
  @Test
  public void fillsAttributeOnCreatedDefaultUrlAlias() throws CudamiServiceException {
    when(slugGeneratorService.apply(any(Locale.class), any(String.class), eq(null)))
        .thenReturn("hallo-welt");

    UUID expectedTargetUuid = UUID.randomUUID();

    Entity entity = new Entity();
    entity.setLabel(new LocalizedText(Locale.GERMAN, "Hallo Welt"));
    entity.setUuid(expectedTargetUuid);
    IdentifiableUrlAliasAlignHelper.checkDefaultAliases(entity, cudamiConfig, slugGeneratorService);

    UrlAlias actual = entity.getLocalizedUrlAliases().flatten().get(0);
    assertThat(actual.getLastPublished()).isNull(); // is set by the repository
    assertThat(actual.isPrimary()).isTrue();
    assertThat(actual.getCreated()).isNull(); // is set by the repository
    assertThat(actual.getTargetUuid()).isEqualTo(expectedTargetUuid);
    assertThat(actual.getTargetIdentifiableType()).isEqualTo(entity.getType());
    assertThat(actual.getTargetEntityType()).isEqualTo(entity.getEntityType());
    assertThat(actual.getWebsite()).isNull(); // no default website given
    assertThat(actual.getSlug()).isEqualTo("hallo-welt");
    assertThat(actual.getTargetLanguage()).isEqualTo(Locale.GERMAN);
  }

  @DisplayName("does not create a default UrlAliases for a webpage")
  @Test
  public void noDefaultUrlAliasCreationForWebpage() throws CudamiServiceException {
    when(slugGeneratorService.apply(any(Locale.class), any(String.class), eq(null)))
        .thenReturn("hallo-welt");
    UUID expectedTargetUuid = UUID.randomUUID();

    Identifiable identifiable = new Webpage();
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "Hallo Welt"));
    identifiable.setUuid(expectedTargetUuid);

    assertThrows(
        CudamiServiceException.class,
        () -> {
          IdentifiableUrlAliasAlignHelper.checkDefaultAliases(
              identifiable, cudamiConfig, slugGeneratorService);
        });

    assertThat(identifiable.getLocalizedUrlAliases().isEmpty());
  }

  @DisplayName("adds an url alias for a certain language, when it's missing")
  @Test
  public void addUrlAliasWhenMissingInLanguage() throws CudamiServiceException {
    when(slugGeneratorService.apply(eq(Locale.GERMAN), any(String.class), eq(null)))
        .thenReturn("hallo-welt");
    when(slugGeneratorService.apply(eq(Locale.ENGLISH), any(String.class), eq(null)))
        .thenReturn("hello-world");

    UUID expectedTargetUuid = UUID.randomUUID();

    Entity entity = new Entity();
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Hallo Welt");
    label.setText(Locale.ENGLISH, "hello world");
    entity.setLabel(label);
    entity.setUuid(expectedTargetUuid);

    // Let's assume, we only have localized aliases for the german label yet
    UrlAlias germanUrlAlias = new UrlAlias();
    germanUrlAlias.setPrimary(true);
    germanUrlAlias.setSlug("hallo-welt");
    germanUrlAlias.setTargetLanguage(Locale.GERMAN);
    germanUrlAlias.setTargetUuid(expectedTargetUuid);
    LocalizedUrlAliases localizedUrlALiases = new LocalizedUrlAliases(germanUrlAlias);
    entity.setLocalizedUrlAliases(localizedUrlALiases);

    IdentifiableUrlAliasAlignHelper.checkDefaultAliases(entity, cudamiConfig, slugGeneratorService);

    assertThat(localizedUrlALiases.flatten()).hasSize(2); // german and english
    assertThat(localizedUrlALiases.hasTargetLanguage(Locale.GERMAN)).isTrue();
    assertThat(localizedUrlALiases.hasTargetLanguage(Locale.ENGLISH)).isTrue();

    assertThat(localizedUrlALiases.get(Locale.ENGLISH).get(0).getSlug()).isEqualTo("hello-world");
  }

  @DisplayName("throws an exception, when primary UrlAliases are missing")
  @Test
  public void missingPrimaryUrlAliases() {
    UUID expectedTargetUuid = UUID.randomUUID();

    Entity entity = new Entity();
    entity.setLabel(new LocalizedText(Locale.GERMAN, "Hallo Welt"));
    entity.setUuid(expectedTargetUuid);

    UrlAlias germanUrlAlias = new UrlAlias();
    germanUrlAlias.setPrimary(false);
    germanUrlAlias.setSlug("hallo-welt");
    germanUrlAlias.setTargetLanguage(Locale.GERMAN);
    germanUrlAlias.setTargetUuid(expectedTargetUuid);
    LocalizedUrlAliases localizedUrlALiases = new LocalizedUrlAliases(germanUrlAlias);
    entity.setLocalizedUrlAliases(localizedUrlALiases);

    assertThrows(
        CudamiServiceException.class,
        () -> {
          IdentifiableUrlAliasAlignHelper.checkDefaultAliases(
              entity, cudamiConfig, slugGeneratorService);
        });
  }

  @DisplayName("throws an Exception, when slug generation fails")
  @Test
  public void failingSlugGeneration() throws CudamiServiceException {
    when(slugGeneratorService.apply(any(Locale.class), any(String.class), eq(null)))
        .thenThrow(new CudamiServiceException("boo"));

    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");

    assertThrows(
        CudamiServiceException.class,
        () -> {
          IdentifiableUrlAliasAlignHelper.checkDefaultAliases(
              identifiable, cudamiConfig, slugGeneratorService);
        });
  }
}
