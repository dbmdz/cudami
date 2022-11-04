package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Identifiable-UrlAlias-Align-Helper tests")
public class IdentifiableUrlAliasAlignHelperTest {

  protected static final Locale LOCALE_UND_LATN =
      new Locale.Builder().setLanguage("und").setScript("Latn").build();
  protected static final Locale LOCALE_UND_HANI =
      new Locale.Builder().setLanguage("und").setScript("Hani").build();

  CudamiConfig cudamiConfig;
  IdentifiableUrlAliasAlignHelper.SlugGeneratorService slugGeneratorService;

  @BeforeEach
  public void setup() {
    CudamiConfig.UrlAlias urlAliasConfig =
        new CudamiConfig.UrlAlias(List.of(IdentifiableObjectType.DIGITAL_OBJECT.toString()), 0);
    cudamiConfig = new CudamiConfig(null, urlAliasConfig, 5000, null);

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
    assertThat(identifiable.getLocalizedUrlAliases().flatten().size()).isEqualTo(1);
    assertThat(identifiable.getLocalizedUrlAliases().flatten().get(0).isPrimary()).isTrue();
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

    assertThat(entity.getLocalizedUrlAliases().flatten().size()).isEqualTo(1);
    UrlAlias actual = entity.getLocalizedUrlAliases().flatten().get(0);
    assertThat(actual.getLastPublished()).isNull(); // is set by the repository
    assertThat(actual.isPrimary()).isTrue();
    assertThat(actual.getCreated()).isNull(); // is set by the repository
    assertThat(actual.getTargetUuid()).isEqualTo(expectedTargetUuid);
    assertThat(actual.getTargetIdentifiableType()).isEqualTo(entity.getType());
    assertThat(actual.getTargetIdentifiableObjectType())
        .isEqualTo(entity.getIdentifiableObjectType());
    assertThat(actual.getWebsite()).isNull(); // no default website given
    assertThat(actual.getSlug()).isEqualTo("hallo-welt");
    assertThat(actual.getTargetLanguage()).isEqualTo(Locale.GERMAN);
  }

  @DisplayName("does not create a default UrlAliases for a webpage")
  @Test
  public void noDefaultUrlAliasCreationForWebpage() throws CudamiServiceException {
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
    assertThat(localizedUrlALiases.get(Locale.ENGLISH).get(0).isPrimary()).isTrue();
  }

  @DisplayName(
      "avoids creating an UrlAlias, when slug and languare are the same and only script differs")
  @Test
  public void doesNotAddUrlAliasWhenOnlyScriptDiffers() throws CudamiServiceException {
    when(slugGeneratorService.apply(eq(LOCALE_UND_LATN), eq("Yu ji shan ren"), eq(null)))
        .thenReturn("yu-ji-shan-ren");
    when(slugGeneratorService.apply(eq(LOCALE_UND_HANI), eq("玉几山人"), eq(null)))
        .thenReturn("yu-ji-shan-ren");

    UUID expectedTargetUuid = UUID.randomUUID();

    Entity entity = new Entity();
    final LocalizedText label = new LocalizedText(LOCALE_UND_LATN, "Yu ji shan ren");
    label.setText(LOCALE_UND_HANI, "玉几山人");
    entity.setLabel(label);
    entity.setUuid(expectedTargetUuid);

    IdentifiableUrlAliasAlignHelper.checkDefaultAliases(entity, cudamiConfig, slugGeneratorService);

    LocalizedUrlAliases localizedUrlAliases = entity.getLocalizedUrlAliases();

    final Locale LOCALE_UND = Locale.forLanguageTag("und");

    assertThat(localizedUrlAliases.flatten())
        .hasSize(1); // only for language "und" (ROOT), but not for combinations with scripts
    assertThat(localizedUrlAliases.hasTargetLanguage(LOCALE_UND)).isTrue();
    assertThat(localizedUrlAliases.hasTargetLanguage(LOCALE_UND_LATN)).isFalse();
    assertThat(localizedUrlAliases.hasTargetLanguage(LOCALE_UND_HANI)).isFalse();

    assertThat(localizedUrlAliases.get(LOCALE_UND).get(0).getSlug()).isEqualTo("yu-ji-shan-ren");
    assertThat(localizedUrlAliases.get(LOCALE_UND).get(0).isPrimary()).isTrue();
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

  @DisplayName("if there are not any aliases then fetch them from object of db")
  @Test
  public void fetchAliasesfromSavedObject() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    dbIdent.setLocalizedUrlAliases(
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID())));

    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isEqualTo(dbIdent);
  }

  @DisplayName("if there are only different primary aliases then unset existing ones - 2 vs. 2")
  @Test
  public void unsetExistingPrimaries() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.GERMAN, "somethingelse", false, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "neuer-alias", true),
            new SlugPrimaryTuple(Locale.ENGLISH, "new-alias", true));
    identifiable.setLocalizedUrlAliases(aliases);

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size())
        .isEqualTo(4); // non primary is absent thus it will be deleted
    assertThat(dbAliases.flatten()).allMatch(ua -> !ua.isPrimary());

    // keep in mind that `aliases` now contains those from db too
    assertThat(locUrlAliases.flatten().stream().filter(ua -> ua.isPrimary()))
        .allMatch(ua -> Pattern.matches("neuer-alias|new-alias", ua.getSlug()))
        .size()
        .isEqualTo(2);
    assertThat(locUrlAliases.flatten().stream().filter(ua -> !ua.isPrimary()))
        .allMatch(ua -> Pattern.matches("hallo-welt|hello-world", ua.getSlug()))
        .size()
        .isEqualTo(2);
  }

  @DisplayName("if there are only different primary aliases then unset existing ones - 1 vs. 2")
  @Test
  public void unsetExistingPrimary() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(targetUuid, new SlugPrimaryTuple(Locale.GERMAN, "neuer-alias", true));
    identifiable.setLocalizedUrlAliases(aliases);

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(3);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isFalse();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("neuer-alias");
              assertThat(ua.isPrimary()).isTrue();
            });
  }

  @DisplayName("2 existing aliases + 1 new alias")
  @Test
  public void addNewAlias() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.get(Locale.GERMAN).get(0).getUuid()),
            new SlugPrimaryTuple(
                Locale.ENGLISH,
                "hello-world",
                true,
                dbAliases.get(Locale.ENGLISH).get(0).getUuid()),
            new SlugPrimaryTuple(Locale.GERMAN, "neuer-alias", false));
    identifiable.setLocalizedUrlAliases(aliases);

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(3);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("neuer-alias");
              assertThat(ua.isPrimary()).isFalse();
            });
  }

  @DisplayName("2 existing aliases + 1 new alias, primary too")
  @Test
  public void addPrimaryAlias() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.get(Locale.GERMAN).get(0).getUuid()),
            new SlugPrimaryTuple(
                Locale.ENGLISH,
                "hello-world",
                true,
                dbAliases.get(Locale.ENGLISH).get(0).getUuid()),
            new SlugPrimaryTuple(Locale.GERMAN, "neuer-alias", true));
    identifiable.setLocalizedUrlAliases(aliases);

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(3);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isFalse();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("neuer-alias");
              assertThat(ua.isPrimary()).isTrue();
            });
  }

  @DisplayName("2 existing aliases, no changes")
  @Test
  public void doNothingIfNoChanges() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.get(Locale.GERMAN).get(0).getUuid()),
            new SlugPrimaryTuple(
                Locale.ENGLISH,
                "hello-world",
                true,
                dbAliases.get(Locale.ENGLISH).get(0).getUuid()));
    identifiable.setLocalizedUrlAliases(aliases);

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isTrue();
            });
  }

  @DisplayName("2 existing aliases, 1 label changed")
  @Test
  public void labelChanged() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello new world"); // different label
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.get(Locale.GERMAN).get(0).getUuid()),
            new SlugPrimaryTuple(
                Locale.ENGLISH,
                "hello-world",
                true,
                dbAliases.get(Locale.ENGLISH).get(0).getUuid()));
    identifiable.setLocalizedUrlAliases(aliases);

    when(slugGeneratorService.apply(eq(Locale.ENGLISH), eq("hello new world"), eq(null)))
        .thenReturn("hello-new-world");

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(3);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isFalse();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-new-world");
              assertThat(ua.isPrimary()).isTrue();
            });
    verify(slugGeneratorService, times(1)).apply(any(), any(), any());
  }

  @DisplayName("2 existing aliases, 1 label changed + new alias already passed")
  @Test
  public void labelChangedWithNewAlias() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello new world"); // different label
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.get(Locale.GERMAN).get(0).getUuid()),
            new SlugPrimaryTuple(
                Locale.ENGLISH,
                "hello-world",
                true,
                dbAliases.get(Locale.ENGLISH).get(0).getUuid()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-new-world", false));
    identifiable.setLocalizedUrlAliases(aliases);

    when(slugGeneratorService.apply(eq(Locale.ENGLISH), eq("hello new world"), eq(null)))
        .thenReturn("hello-new-world");

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(3);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-new-world");
              assertThat(ua.isPrimary()).isFalse();
            });
    verify(slugGeneratorService, times(1)).apply(any(), any(), any());
  }

  @DisplayName(
      "3 existing aliases, 1 label changed + new alias already passed but invalid primary states")
  @Test
  public void labelChangedWithNewAliasInvalidPrimaries() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbLabels.setText(Locale.ENGLISH, "hello world");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true, UUID.randomUUID()),
            new SlugPrimaryTuple(Locale.ENGLISH, "helloworld", false, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello new world"); // different label
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.get(Locale.GERMAN).get(0).getUuid()),
            new SlugPrimaryTuple(
                Locale.ENGLISH,
                "hello-world",
                true,
                dbAliases.get(Locale.ENGLISH).get(0).getUuid()),
            new SlugPrimaryTuple(
                Locale.ENGLISH,
                "helloworld",
                false,
                dbAliases.get(Locale.ENGLISH).get(1).getUuid()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-new-world", true));
    identifiable.setLocalizedUrlAliases(aliases);

    when(slugGeneratorService.apply(eq(Locale.ENGLISH), eq("hello new world"), eq(null)))
        .thenReturn("hello-new-world");

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(4);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isFalse();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("helloworld");
              assertThat(ua.isPrimary()).isFalse();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-new-world");
              assertThat(ua.isPrimary()).isTrue();
            });
    verify(slugGeneratorService, times(1)).apply(any(), any(), any());
  }

  @DisplayName("1 existing alias, 1 new label -> automatically create new alias")
  @Test
  public void addLabel() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid, new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.flatten().get(0).getUuid()));
    identifiable.setLocalizedUrlAliases(aliases);

    when(slugGeneratorService.apply(eq(Locale.ENGLISH), eq("hello world"), eq(null)))
        .thenReturn("hello-world");

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isTrue();
            });
    verify(slugGeneratorService, times(1)).apply(any(), any(), any());
  }

  @DisplayName("1 existing alias, 1 new label + fitting alias")
  @Test
  public void addLabelAndAlias() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    // DB
    Identifiable dbIdent = new Identifiable();
    LocalizedText dbLabels = new LocalizedText(Locale.GERMAN, "hallo welt");
    dbIdent.setLabel(dbLabels);
    LocalizedUrlAliases dbAliases =
        createAliases(
            targetUuid, new SlugPrimaryTuple(Locale.GERMAN, "hallo-welt", true, UUID.randomUUID()));
    dbIdent.setLocalizedUrlAliases(dbAliases);

    // Update
    Identifiable identifiable = new Identifiable();
    LocalizedText labels = new LocalizedText(Locale.GERMAN, "hallo welt");
    labels.setText(Locale.ENGLISH, "hello world");
    identifiable.setLabel(labels);
    LocalizedUrlAliases aliases =
        createAliases(
            targetUuid,
            new SlugPrimaryTuple(
                Locale.GERMAN, "hallo-welt", true, dbAliases.flatten().get(0).getUuid()),
            new SlugPrimaryTuple(Locale.ENGLISH, "hello-world", true));
    identifiable.setLocalizedUrlAliases(aliases);

    when(slugGeneratorService.apply(eq(Locale.ENGLISH), eq("hello world"), eq(null)))
        .thenReturn("hello-world");

    IdentifiableUrlAliasAlignHelper.alignForUpdate(
        identifiable, dbIdent, cudamiConfig, slugGeneratorService);

    assertThat(identifiable).isNotEqualTo(dbIdent);

    LocalizedUrlAliases locUrlAliases = identifiable.getLocalizedUrlAliases();
    assertThat(locUrlAliases.size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten().size()).isEqualTo(2);
    assertThat(locUrlAliases.flatten())
        .satisfiesExactlyInAnyOrder(
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hallo-welt");
              assertThat(ua.isPrimary()).isTrue();
            },
            ua -> {
              assertThat(ua.getSlug()).isEqualTo("hello-world");
              assertThat(ua.isPrimary()).isTrue();
            });
    verify(slugGeneratorService, never()).apply(any(), any(), any());
  }

  private LocalizedUrlAliases createAliases(UUID target, SlugPrimaryTuple... slugTuples) {
    LocalizedUrlAliases aliases = new LocalizedUrlAliases();
    for (var tuple : slugTuples) {
      UrlAlias alias = new UrlAlias();
      alias.setSlug(tuple.slug);
      alias.setPrimary(tuple.isPrimary);
      alias.setTargetUuid(target);
      alias.setTargetLanguage(tuple.lang);
      alias.setUuid(tuple.uuid);
      aliases.add(alias);
    }
    return aliases;
  }

  class SlugPrimaryTuple {
    public Locale lang;
    public String slug;
    public boolean isPrimary;
    public UUID uuid;

    public SlugPrimaryTuple(Locale lang, String slug, boolean isPrimary, UUID aliasUuid) {
      this.lang = lang;
      this.slug = slug;
      this.isPrimary = isPrimary;
      this.uuid = aliasUuid;
    }

    public SlugPrimaryTuple(Locale lang, String slug, boolean isPrimary) {
      this(lang, slug, isPrimary, null);
    }
  }
}
