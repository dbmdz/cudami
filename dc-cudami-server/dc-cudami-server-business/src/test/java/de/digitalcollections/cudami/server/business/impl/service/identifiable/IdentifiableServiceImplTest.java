package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Identifiable Service")
class IdentifiableServiceImplTest {

  private IdentifiableServiceImpl service;
  private IdentifiableRepository repo;
  private UrlAliasService urlAliasService;
  private IdentifierRepository identifierRepository;

  @BeforeEach
  public void beforeEach() {
    repo = mock(IdentifiableRepository.class);
    urlAliasService = mock(UrlAliasService.class);
    identifierRepository = mock(IdentifierRepository.class);
    CudamiConfig cudamiConfig = new CudamiConfig();
    CudamiConfig.UrlAlias urlAliasConfig = new CudamiConfig.UrlAlias();
    urlAliasConfig.setGenerationExcludes(List.of(EntityType.DIGITAL_OBJECT));
    cudamiConfig.setUrlAlias(urlAliasConfig);
    service =
        new IdentifiableServiceImpl(repo, identifierRepository, urlAliasService, cudamiConfig);
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
  @Test
  public void noUrlAliasesForSomeEntityTypes() throws IdentifiableServiceException {
    DigitalObject identifiable = new DigitalObject();
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

  @DisplayName("does not create a default UrlAliases for a webpage")
  @Test
  public void noDefaultUrlAliasCreationForWebpage()
      throws CudamiServiceException, IdentifiableServiceException {
    when(urlAliasService.generateSlug(any(Locale.class), any(String.class), eq(null)))
        .thenReturn("hallo-welt");
    UUID expectedTargetUuid = UUID.randomUUID();

    Identifiable identifiable = new Webpage();
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "Hallo Welt"));
    identifiable.setUuid(expectedTargetUuid);

    service.ensureDefaultAliasesExist(identifiable);

    assertThat(identifiable.getLocalizedUrlAliases().isEmpty());
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
      throws IdentifiableServiceException, CudamiServiceException, ValidationException {
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
      throws IdentifiableServiceException, CudamiServiceException, ValidationException {
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

    when(urlAliasService.findPrimaryLinksForTarget(any())).thenReturn(new ArrayList<>());

    service.update(identifiable);
    verify(urlAliasService, times(1)).deleteAllForTarget(eq(targetUuid));
  }

  @DisplayName("updates existing UrlAliases on an identifiable, when updating it")
  @Test
  public void updateExistingUrlAliasesOnUpdate()
      throws IdentifiableServiceException, CudamiServiceException, ValidationException {
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
    when(urlAliasService.findPrimaryLinksForTarget(eq(targetUuid))).thenReturn(List.of(urlAlias));

    service.update(identifiable);

    verify(urlAliasService, times(1)).update(eq(urlAlias));
  }

  @DisplayName("creates missing UrlAliases on an identifiable, when updating it")
  @Test
  public void createMissingUrlAliasesOnUpdate()
      throws IdentifiableServiceException, CudamiServiceException, ValidationException {
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
  public void exceptionOnMultiplePrimaryEntries()
      throws CudamiServiceException, ValidationException {
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

    when(urlAliasService.findPrimaryLinksForTarget(any())).thenReturn(new ArrayList<>());
    doThrow(new ValidationException("no way!"))
        .when(urlAliasService)
        .validate(eq(localizedUrlAliases));

    assertThrows(
        ValidationException.class,
        () -> {
          service.update(identifiable);
        });
  }

  @DisplayName(
      "throws an exception, when two primary entries for the same (null,target,language) tuple are set")
  @Test
  public void exceptionOnMultiplePrimaryEntriesBasedOnSlug()
      throws CudamiServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(null);
    when(urlAliasService.findPrimaryLinksForTarget(any())).thenReturn(new ArrayList<>());

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
        ValidationException.class,
        () -> {
          service.update(identifiable);
        });
  }

  @DisplayName("allows two primary entries for different (website,target,language) tuples")
  @Test
  public void allowMultiplePrimariesForDifferentTuples()
      throws CudamiServiceException, IdentifiableServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(null);
    when(urlAliasService.findPrimaryLinksForTarget(any())).thenReturn(new ArrayList<>());

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

  @DisplayName("update Identifiable w/o localizedUrlAliases")
  @Test
  public void updateWithoutUrlAliases()
      throws CudamiServiceException, IdentifiableServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondPrimaryUrlAlias.setCreated(LocalDateTime.now());
    secondPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondPrimaryUrlAlias.setSlug("slug2");

    localizedUrlAliases.add(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(localizedUrlAliases);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiable.setLabel(label);

    Identifiable expected = new Identifiable();
    expected.setUuid(targetUuid);
    expected.setLabel(label);
    expected.setLocalizedUrlAliases(localizedUrlAliases);

    when(repo.update(eq(identifiable))).thenReturn(identifiable);

    assertThat(service.update(identifiable)).isEqualTo(expected);
    verify(urlAliasService, never()).create(any(), eq(true));
    verify(urlAliasService, times(2)).update(any());
  }

  @DisplayName("update Identifiable with different primary localizedUrlAliases only")
  @Test
  public void updateWithPrimaryUrlAliasesOnly()
      throws CudamiServiceException, IdentifiableServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

    // in DB (should be unset)
    LocalizedUrlAliases storedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstStoredPrimaryUrlAlias = new UrlAlias();
    firstStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setPrimary(true);
    firstStoredPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstStoredPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstStoredPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondStoredPrimaryUrlAlias = new UrlAlias();
    secondStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setPrimary(true);
    secondStoredPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondStoredPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondStoredPrimaryUrlAlias.setSlug("slug2");

    storedUrlAliases.add(firstStoredPrimaryUrlAlias, secondStoredPrimaryUrlAlias);

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(storedUrlAliases);
    when(urlAliasService.findPrimaryLinksForTarget(eq(targetUuid)))
        .thenReturn(List.of(firstStoredPrimaryUrlAlias, secondStoredPrimaryUrlAlias));

    // new ones
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstPrimaryUrlAlias.setSlug("slug-de-new");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondPrimaryUrlAlias.setSlug("slug-en-new");

    localizedUrlAliases.add(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiable.setLabel(label);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    when(repo.update(eq(identifiable))).thenReturn(identifiable);
    service.update(identifiable);

    verify(urlAliasService, times(2)).create(any(), eq(true));
    verify(urlAliasService, times(2)).update(any());
    verify(urlAliasService).create(eq(firstPrimaryUrlAlias), eq(true));
    verify(urlAliasService).create(eq(secondPrimaryUrlAlias), eq(true));
    // the stored UrlAliases must have been changed to primary == false
    assertThat(firstStoredPrimaryUrlAlias.isPrimary()).isFalse();
    assertThat(secondStoredPrimaryUrlAlias.isPrimary()).isFalse();
    verify(urlAliasService).update(eq(firstStoredPrimaryUrlAlias));
    verify(urlAliasService).update(eq(secondStoredPrimaryUrlAlias));
  }

  @DisplayName("update Identifiable with only one different primary UrlAlias")
  @Test
  public void updateWithOnePrimaryUrlAliasOnly()
      throws CudamiServiceException, IdentifiableServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

    // in DB
    LocalizedUrlAliases storedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstStoredPrimaryUrlAlias = new UrlAlias();
    firstStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setPrimary(true);
    firstStoredPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstStoredPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstStoredPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondStoredPrimaryUrlAlias = new UrlAlias();
    secondStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setPrimary(true);
    secondStoredPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondStoredPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondStoredPrimaryUrlAlias.setSlug("slug2");

    storedUrlAliases.add(firstStoredPrimaryUrlAlias, secondStoredPrimaryUrlAlias);

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(storedUrlAliases);
    when(urlAliasService.findPrimaryLinksForTarget(eq(targetUuid)))
        .thenReturn(List.of(firstStoredPrimaryUrlAlias, secondStoredPrimaryUrlAlias));

    // new one
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstPrimaryUrlAlias.setSlug("slug-de-new");

    localizedUrlAliases.add(firstPrimaryUrlAlias);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiable.setLabel(label);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    when(repo.update(eq(identifiable))).thenReturn(identifiable);
    service.update(identifiable);

    verify(urlAliasService, times(1)).create(any(), eq(true));
    verify(urlAliasService, times(2)).update(any());
    verify(urlAliasService).create(eq(firstPrimaryUrlAlias), eq(true));
    // the german stored UrlAliases must have been changed to primary == false
    assertThat(firstStoredPrimaryUrlAlias.isPrimary()).isFalse();
    assertThat(secondStoredPrimaryUrlAlias.isPrimary()).isTrue();
    verify(urlAliasService).update(eq(firstStoredPrimaryUrlAlias));
    verify(urlAliasService).update(eq(secondStoredPrimaryUrlAlias));
  }

  @DisplayName("update Identifiable with new language and primary UrlAlias")
  @Test
  public void updateWithAdditionalLanguage()
      throws CudamiServiceException, IdentifiableServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

    // in DB
    UrlAlias firstStoredPrimaryUrlAlias = new UrlAlias();
    firstStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setPrimary(true);
    firstStoredPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstStoredPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstStoredPrimaryUrlAlias.setSlug("slug1");

    LocalizedUrlAliases storedUrlAliases = new LocalizedUrlAliases(firstStoredPrimaryUrlAlias);

    when(urlAliasService.findLocalizedUrlAliases(eq(targetUuid))).thenReturn(storedUrlAliases);
    when(urlAliasService.findPrimaryLinksForTarget(eq(targetUuid)))
        .thenReturn(List.of(firstStoredPrimaryUrlAlias));

    // aliases in object to update
    UrlAlias firstPrimaryUrlAlias = new UrlAlias(); // equals to DB
    firstPrimaryUrlAlias.setUuid(firstStoredPrimaryUrlAlias.getUuid());
    firstPrimaryUrlAlias.setCreated(firstStoredPrimaryUrlAlias.getCreated());
    firstPrimaryUrlAlias.setLastPublished(firstStoredPrimaryUrlAlias.getLastPublished());
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTargetUuid(targetUuid);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setTargetUuid(targetUuid);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondPrimaryUrlAlias.setSlug("slug2");

    LocalizedUrlAliases localizedUrlAliases =
        new LocalizedUrlAliases(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiable.setLabel(label);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    when(repo.update(eq(identifiable))).thenReturn(identifiable);
    service.update(identifiable);

    verify(urlAliasService, times(1)).create(eq(secondPrimaryUrlAlias), eq(true));
    verify(urlAliasService, times(1)).update(eq(firstPrimaryUrlAlias));
    assertThat(firstStoredPrimaryUrlAlias.isPrimary()).isTrue();
  }
}
