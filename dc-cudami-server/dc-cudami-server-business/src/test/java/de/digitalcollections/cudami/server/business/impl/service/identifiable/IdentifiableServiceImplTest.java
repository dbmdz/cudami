package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.config.UrlAliasGenerationProperties;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Identifiable Service")
class IdentifiableServiceImplTest {

  private IdentifiableServiceImpl service;
  private IdentifiableRepository repo;
  private UrlAliasService urlAliasService;

  @BeforeEach
  public void beforeEach() {
    repo = mock(IdentifiableRepository.class);
    urlAliasService = mock(UrlAliasService.class);
    service = new IdentifiableServiceImpl(repo);
    service.setUrlAliasService(urlAliasService);
    var aliasGenerationProps = new UrlAliasGenerationProperties();
    aliasGenerationProps.setGenerationExcludes(List.of(EntityType.DIGITAL_OBJECT));
    service.setAliasGenerationProperties(aliasGenerationProps);
  }

  @DisplayName("can add related entities by delegating it to the repository")
  @Test
  public void addRelatedEntity() {
    service.addRelatedEntity(UUID.randomUUID(), UUID.randomUUID());
    verify(repo, times(1)).addRelatedEntity(any(UUID.class), any(UUID.class));
  }

  @DisplayName("can add related fileresources by delegating it to the repository")
  @Test
  public void addRelatedFileResources() {
    service.addRelatedFileresource(UUID.randomUUID(), UUID.randomUUID());
    verify(repo, times(1)).addRelatedFileresource(any(UUID.class), any(UUID.class));
  }

  @DisplayName("can return the number of identifiables")
  @Test
  public void count() {
    when(repo.count()).thenReturn(42L);
    assertThat(service.count()).isEqualTo(42);
  }

  @DisplayName("deletes UrlAliases, too")
  @Test
  public void deleteIncludesUrlAliaess()
      throws IdentifiableServiceException, CudamiServiceException {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    List<UUID> uuids = List.of(uuid1, uuid2);

    service.delete(uuids);
    verify(urlAliasService, times(1)).deleteAllForTarget(eq(uuid1), eq(true));
    verify(urlAliasService, times(1)).deleteAllForTarget(eq(uuid2), eq(true));
    verify(repo, times(1)).delete(eq(uuids));
  }

  @DisplayName(
      "throws an exception to trigger the rollback, when an exception during deletion happens")
  @Test
  public void throwExceptionWhenDeletionFails() throws CudamiServiceException {
    when(urlAliasService.deleteAllForTarget(any(UUID.class), eq(true)))
        .thenThrow(new CudamiServiceException("boo"));
    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.delete(List.of(UUID.randomUUID()));
        });
  }

  @DisplayName("does not guarantee URLAliases for some entity types")
  @Disabled("Logics for skipping URLALias genration for some types is not yet implemented")
  @Test
  public void noUrlAliasesForSomeEntityTypes() throws IdentifiableServiceException {
    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");
    service.ensureDefaultAliasesExist(identifiable);
    assertThat(identifiable.getLocalizedUrlAliases()).isNull();
  }

  @DisplayName("can create an LocalizedUrlAlias, when it's missing")
  @Test
  public void createLocalizedUrlAliasWhenMissing() throws IdentifiableServiceException {
    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");
    service.ensureDefaultAliasesExist(identifiable);
    assertThat(identifiable.getLocalizedUrlAliases()).isNotNull();
  }

  @DisplayName("sets all required attribute of a created default UrlAlias")
  @Test
  public void fillsAttributeOnCreatedDefaultUrlAlias()
      throws IdentifiableServiceException, CudamiServiceException {
    when(urlAliasService.generateSlug(any(Locale.class), any(String.class), eq(null)))
        .thenReturn("hallo-welt");

    UUID expectedTargetUuid = UUID.randomUUID();

    Entity entity = new Entity();
    entity.setLabel(new LocalizedText(Locale.GERMAN, "Hallo Welt"));
    entity.setUuid(expectedTargetUuid);
    service.ensureDefaultAliasesExist(entity);

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

  @DisplayName("adds an url alias for a certain language, when it's missing")
  @Test
  public void addUrlAliasWhenMissingInLanguage()
      throws CudamiServiceException, IdentifiableServiceException {
    when(urlAliasService.generateSlug(eq(Locale.GERMAN), any(String.class), eq(null)))
        .thenReturn("hallo-welt");
    when(urlAliasService.generateSlug(eq(Locale.ENGLISH), any(String.class), eq(null)))
        .thenReturn("hello-world");

    UUID expectedTargetUuid = UUID.randomUUID();

    Entity entity = new Entity();
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Hallo Welt");
    label.setText(Locale.ENGLISH, "hello world");
    entity.setLabel(label);
    entity.setUuid(expectedTargetUuid);

    // Let's assume, we only have localized aliases for the german label yet
    LocalizedUrlAliases localizedUrlALiases = new LocalizedUrlAliases();
    UrlAlias germanUrlAlias = new UrlAlias();
    germanUrlAlias.setPrimary(true);
    germanUrlAlias.setSlug("hallo-welt");
    germanUrlAlias.setTargetLanguage(Locale.GERMAN);
    germanUrlAlias.setTargetUuid(expectedTargetUuid);
    localizedUrlALiases.add(germanUrlAlias);
    entity.setLocalizedUrlAliases(localizedUrlALiases);

    service.ensureDefaultAliasesExist(entity);

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

    LocalizedUrlAliases localizedUrlALiases = new LocalizedUrlAliases();
    UrlAlias germanUrlAlias = new UrlAlias();
    germanUrlAlias.setPrimary(false);
    germanUrlAlias.setSlug("hallo-welt");
    germanUrlAlias.setTargetLanguage(Locale.GERMAN);
    germanUrlAlias.setTargetUuid(expectedTargetUuid);
    localizedUrlALiases.add(germanUrlAlias);
    entity.setLocalizedUrlAliases(localizedUrlALiases);

    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.ensureDefaultAliasesExist(entity);
        });
  }

  @DisplayName("throws an Exception, when slug generation fails")
  @Test
  public void failingSlugGeneration() throws CudamiServiceException {
    when(urlAliasService.generateSlug(any(Locale.class), any(String.class), eq(null)))
        .thenThrow(new CudamiServiceException("boo"));

    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");

    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.ensureDefaultAliasesExist(identifiable);
        });
  }

  @DisplayName("throws an Exception to trigger a rollback on save, when saving in the repo fails")
  @Test
  public void exceptionOnSaveWhenRepoFails() {
    when(repo.save(any(Identifiable.class))).thenThrow(new NullPointerException("boo"));

    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.save(new Identifiable());
        });
  }

  @DisplayName(
      "throws an Exception to trigger a rollback on save, when creating and saving an UrlAlias fails")
  @Test
  public void exceptionOnSaveWhenSavingUrlAliasFails() throws CudamiServiceException {
    when(urlAliasService.create(any(UrlAlias.class))).thenThrow(new CudamiServiceException("boo"));

    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");

    when(repo.save(any(Identifiable.class))).thenReturn(identifiable);
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setPrimary(true);
    urlAlias.setSlug("label");

    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.save(identifiable);
        });
  }

  @DisplayName("can save Identifiables without UrlAliases and creates an UrlAlias for them")
  @Test
  public void saveIdentifiableWithoutUrlAliases()
      throws IdentifiableServiceException, CudamiServiceException {
    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");

    when(repo.save(any(Identifiable.class))).thenReturn(identifiable);
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setPrimary(true);
    urlAlias.setSlug("label");
    when(urlAliasService.create(any(UrlAlias.class))).thenReturn(urlAlias);

    Identifiable actual = service.save(identifiable);
    assertThat(actual).isNotNull();

    verify(repo, times(1)).save(any(Identifiable.class));
    verify(urlAliasService, times(1)).create(any(UrlAlias.class));
  }

  @DisplayName(
      "throws an Exception to trigger a rollback on update, when updating in the repo fails")
  @Test
  public void exceptionOnUpdateWhenRepoFails() {
    when(repo.update(any(Identifiable.class))).thenThrow(new NullPointerException("boo"));

    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.update(new Identifiable());
        });
  }

  @DisplayName(
      "deletes all connected UrlAliases on an identifiable as first step when updating an identifiable")
  @Test
  public void deleteUrlAliasesOnUpdate()
      throws IdentifiableServiceException, CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "label"));

    when(repo.update(eq(identifiable))).thenReturn(identifiable);

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setPrimary(true);
    urlAlias.setSlug("label");
    urlAlias.setTargetUuid(targetUuid);
    localizedUrlAliases.add(urlAlias);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    service.update(identifiable);
    verify(urlAliasService, times(1)).deleteAllForTarget(eq(targetUuid));
  }

  @DisplayName("updates existing UrlAliases on an identifiable, when updating it")
  @Test
  public void updateExistingUrlAliasesOnUpdate()
      throws IdentifiableServiceException, CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "label"));

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
    urlAlias.setPrimary(true);
    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setSlug("label");
    urlAlias.setTargetLanguage(Locale.GERMAN);
    urlAlias.setTargetUuid(targetUuid);
    localizedUrlAliases.add(urlAlias);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);
    when(repo.update(eq(identifiable))).thenReturn(identifiable);

    service.update(identifiable);

    verify(urlAliasService, times(1)).update(eq(urlAlias));
  }

  @DisplayName("creates missing UrlAliases on an identifiable, when updating it")
  @Test
  public void createMissingUrlAliasesOnUpdate()
      throws IdentifiableServiceException, CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "label"));

    when(repo.update(eq(identifiable))).thenReturn(identifiable);

    service.update(identifiable);

    verify(urlAliasService, times(1)).create(any(UrlAlias.class), eq(true));
  }

  @DisplayName(
      "throws an exception, when two primary entries for the same (website,target,language) tuple are set")
  @Test
  public void exceptionOnMultiplePrimaryEntries() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(null);

    Website website = new Website();
    website.setUuid(UUID.randomUUID());

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setWebsite(website);
    firstPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setWebsite(website);
    secondPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    secondPrimaryUrlAlias.setSlug("slug2");

    localizedUrlAliases.add(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);
    identifiable.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "slug"));

    doThrow(new ValidationException("no way!"))
        .when(urlAliasService)
        .validate(eq(localizedUrlAliases));

    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.update(identifiable);
        });
  }

  @DisplayName(
      "throws an exception, when two primary entries for the same (null,target,language) tuple are set")
  @Test
  public void exceptionOnMultiplePrimaryEntriesBasedOnSlug() throws CudamiServiceException {
    UUID targetUuid = UUID.randomUUID();

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(null);

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setWebsite(null);
    firstPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setWebsite(null);
    secondPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    secondPrimaryUrlAlias.setSlug("slug2");

    localizedUrlAliases.add(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);
    identifiable.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "slug"));

    doThrow(new ValidationException("no way!"))
        .when(urlAliasService)
        .validate(eq(localizedUrlAliases));

    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.update(identifiable);
        });
  }

  @DisplayName("allows two primary entries for different (website,target,language) tuples")
  @Test
  public void allowMultiplePrimariesForDifferentTuples()
      throws CudamiServiceException, IdentifiableServiceException {
    UUID targetUuid = UUID.randomUUID();

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(null);

    Website website = new Website();
    website.setUuid(UUID.randomUUID());

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setWebsite(website);
    firstPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setWebsite(website);
    secondPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("en"));
    secondPrimaryUrlAlias.setSlug("slug2");

    localizedUrlAliases.add(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    LocalizedText label = new LocalizedText(Locale.forLanguageTag("de"), "label");
    label.setText(Locale.forLanguageTag("en"), "label");
    identifiable.setLabel(label);

    when(repo.update(identifiable)).thenReturn(identifiable);

    service.update(identifiable);
  }
}
