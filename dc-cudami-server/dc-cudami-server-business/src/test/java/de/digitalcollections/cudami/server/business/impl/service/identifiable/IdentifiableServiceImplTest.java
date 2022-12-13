package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("The Identifiable Service")
class IdentifiableServiceImplTest {

  protected static final Locale LOCALE_UND_LATN =
      new Locale.Builder().setLanguage("und").setScript("Latn").build();
  protected static final Locale LOCALE_UND_HANI =
      new Locale.Builder().setLanguage("und").setScript("Hani").build();

  private IdentifiableServiceImpl service;
  private IdentifiableRepository repo;
  private UrlAliasService urlAliasService;
  private IdentifierService identifierService;

  @BeforeEach
  public void beforeEach() throws CudamiServiceException {
    repo = mock(IdentifiableRepository.class);
    urlAliasService = mock(UrlAliasService.class);
    when(urlAliasService.generateSlug(any(), eq("label"), eq(null))).thenReturn("label");
    identifierService = mock(IdentifierService.class);
    CudamiConfig.UrlAlias urlAliasConfig = new CudamiConfig.UrlAlias(List.of("DigitalObject"), 0);

    CudamiConfig cudamiConfig = Mockito.mock(CudamiConfig.class);
    when(cudamiConfig.getOffsetForAlternativePaging()).thenReturn(5000);
    when(cudamiConfig.getUrlAlias()).thenReturn(urlAliasConfig);

    LocaleService localeService = mock(LocaleService.class);

    service =
        new IdentifiableServiceImpl(
            repo, identifierService, urlAliasService, localeService, cudamiConfig);
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

  @DisplayName("throws an Exception to trigger a rollback on save, when saving in the repo fails")
  @Test
  public void exceptionOnSaveWhenRepoFails() throws RepositoryException {
    doThrow(NullPointerException.class).when(repo).save(any(Identifiable.class));

    Identifiable identifiable = Identifiable.builder().label("label").build();
    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.save(identifiable);
        });
  }

  @DisplayName(
      "throws an Exception to trigger a rollback on save, when creating and saving an UrlAlias fails")
  @Test
  public void exceptionOnSaveWhenSavingUrlAliasFails() throws CudamiServiceException {
    doThrow(CudamiServiceException.class).when(urlAliasService).save(any(UrlAlias.class));

    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");

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
      throws ServiceException, CudamiServiceException, ValidationException, RepositoryException {
    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");

    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setPrimary(true);
    urlAlias.setSlug("label");

    service.save(identifiable);
    assertThat(identifiable).isNotNull();

    verify(repo, times(1)).save(any(Identifiable.class));
    verify(urlAliasService, times(1)).save(any(UrlAlias.class));
  }

  @DisplayName(
      "throws an Exception to trigger a rollback on update, when updating in the repo fails")
  @Test
  public void exceptionOnUpdateWhenRepoFails() throws RepositoryException {
    doThrow(NullPointerException.class).when(repo).update(any(Identifiable.class));

    Identifiable identifiable = Identifiable.builder().label("label").build();
    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.update(identifiable);
        });
  }

  @DisplayName(
      "deletes all connected UrlAliases on an identifiable as first step when updating an identifiable")
  @Test
  public void deleteUrlAliasesOnUpdate()
      throws ServiceException, CudamiServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "label"));

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    identifiableInDb.setLabel(new LocalizedText(Locale.GERMAN, "label"));

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setPrimary(true);
    urlAlias.setSlug("label");
    urlAlias.setTargetUuid(targetUuid);
    localizedUrlAliases.add(urlAlias);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    when(repo.getByUuid(eq(targetUuid))).thenReturn(identifiableInDb);

    service.update(identifiable);
    verify(urlAliasService, times(1)).deleteAllForTarget(eq(targetUuid));
  }

  @DisplayName("updates existing UrlAliases on an identifiable, when updating it")
  @Test
  public void updateExistingUrlAliasesOnUpdate()
      throws ServiceException, CudamiServiceException, ValidationException {
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
    when(repo.getByUuid(eq(targetUuid))).thenReturn(identifiable);

    service.update(identifiable);

    verify(urlAliasService, times(1)).update(eq(urlAlias));
  }

  @DisplayName("creates missing UrlAliases on an identifiable, when updating it")
  @Test
  public void createMissingUrlAliasesOnUpdate()
      throws ServiceException, CudamiServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "label"));

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    identifiableInDb.setLabel(new LocalizedText(Locale.GERMAN, "oldlabel"));

    when(repo.getByUuid(eq(targetUuid))).thenReturn(identifiableInDb);

    service.update(identifiable);

    verify(urlAliasService, times(1)).save(any(UrlAlias.class), eq(true));
  }

  @DisplayName(
      "throws an exception, when two primary entries for the same (website,target,language) tuple are set")
  @Test
  public void exceptionOnMultiplePrimaryEntries()
      throws CudamiServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

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

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    identifiableInDb.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "slug"));

    when(repo.getByUuid(any(UUID.class))).thenReturn(identifiableInDb);

    when(urlAliasService.getPrimaryUrlAliasesForTarget(any())).thenReturn(new ArrayList<>());
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

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    identifiableInDb.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "slug"));

    when(repo.getByUuid(any(UUID.class))).thenReturn(identifiableInDb);

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
      throws CudamiServiceException, ServiceException, ValidationException {
    UUID targetUuid = UUID.randomUUID();

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
    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    identifiableInDb.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "label"));

    when(repo.getByUuid(any(UUID.class))).thenReturn(identifiableInDb);

    service.update(identifiable);
  }

  @DisplayName("update Identifiable w/o localizedUrlAliases")
  @Test
  public void updateWithoutUrlAliases()
      throws CudamiServiceException, ServiceException, ValidationException {
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

    when(urlAliasService.getLocalizedUrlAliases(eq(targetUuid))).thenReturn(localizedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText labelDb = new LocalizedText(Locale.GERMAN, "label");
    labelDb.setText(Locale.ENGLISH, "label");
    identifiableInDb.setLabel(labelDb);
    identifiableInDb.setLocalizedUrlAliases(localizedUrlAliases);

    when(repo.getByUuid(eq(targetUuid))).thenReturn(identifiableInDb);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiable.setLabel(label);

    Identifiable expected = new Identifiable();
    expected.setUuid(targetUuid);
    expected.setLabel(label);
    expected.setLocalizedUrlAliases(localizedUrlAliases);

    service.update(identifiable);
    assertThat(identifiable).isEqualTo(expected);
    verify(urlAliasService, never()).save(any(), eq(true));
    verify(urlAliasService, times(2)).update(any());
  }

  @DisplayName("update Identifiable with different primary localizedUrlAliases only")
  @Test
  public void updateWithPrimaryUrlAliasesOnly()
      throws CudamiServiceException, ServiceException, ValidationException {
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

    when(urlAliasService.getLocalizedUrlAliases(eq(targetUuid))).thenReturn(storedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiableInDb.setLabel(label);
    identifiableInDb.setLocalizedUrlAliases(storedUrlAliases);

    when(repo.getByUuid(eq(targetUuid))).thenReturn(identifiableInDb);

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
    identifiable.setLabel(label);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    service.update(identifiable);

    verify(urlAliasService, times(2)).save(any(), eq(true));
    verify(urlAliasService, times(2)).update(any());
    verify(urlAliasService).save(eq(firstPrimaryUrlAlias), eq(true));
    verify(urlAliasService).save(eq(secondPrimaryUrlAlias), eq(true));
    // the stored UrlAliases must have been changed to primary == false
    assertThat(firstStoredPrimaryUrlAlias.isPrimary()).isFalse();
    assertThat(secondStoredPrimaryUrlAlias.isPrimary()).isFalse();
    verify(urlAliasService).update(eq(firstStoredPrimaryUrlAlias));
    verify(urlAliasService).update(eq(secondStoredPrimaryUrlAlias));
  }

  @DisplayName("update Identifiable with only one different primary UrlAlias")
  @Test
  public void updateWithOnePrimaryUrlAliasOnly()
      throws CudamiServiceException, ServiceException, ValidationException {
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

    when(urlAliasService.getLocalizedUrlAliases(eq(targetUuid))).thenReturn(storedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiableInDb.setLabel(label);
    identifiableInDb.setLocalizedUrlAliases(storedUrlAliases);

    when(repo.getByUuid(eq(targetUuid))).thenReturn(identifiableInDb);

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
    identifiable.setLabel(label);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    service.update(identifiable);

    verify(urlAliasService, times(1)).save(any(), eq(true));
    verify(urlAliasService, times(2)).update(any());
    verify(urlAliasService).save(eq(firstPrimaryUrlAlias), eq(true));
    // the german stored UrlAliases must have been changed to primary == false
    assertThat(firstStoredPrimaryUrlAlias.isPrimary()).isFalse();
    assertThat(secondStoredPrimaryUrlAlias.isPrimary()).isTrue();
    verify(urlAliasService).update(eq(firstStoredPrimaryUrlAlias));
    verify(urlAliasService).update(eq(secondStoredPrimaryUrlAlias));
  }

  @DisplayName("update Identifiable with new language and primary UrlAlias")
  @Test
  public void updateWithAdditionalLanguage()
      throws CudamiServiceException, ServiceException, ValidationException {
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

    when(urlAliasService.getLocalizedUrlAliases(eq(targetUuid))).thenReturn(storedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText labelDb = new LocalizedText(Locale.GERMAN, "label");
    identifiableInDb.setLabel(labelDb);
    identifiableInDb.setLocalizedUrlAliases(storedUrlAliases);

    when(repo.getByUuid(eq(targetUuid))).thenReturn(identifiableInDb);

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

    service.update(identifiable);

    verify(urlAliasService, times(1)).save(eq(secondPrimaryUrlAlias), eq(true));
    verify(urlAliasService, times(1)).update(eq(firstPrimaryUrlAlias));
    assertThat(firstStoredPrimaryUrlAlias.isPrimary()).isTrue();
  }

  @DisplayName("Filters out non-provided identifiers on update")
  @Test
  void updateRemovedNonProvidedIdentifiers()
      throws ValidationException, ServiceException, CudamiServiceException {
    UUID uuid = UUID.randomUUID();

    Identifiable identifiableToUpdate = new Identifiable();
    identifiableToUpdate.setUuid(uuid);
    identifiableToUpdate.setLabel(new LocalizedText(Locale.GERMAN, "Label"));

    UUID identifierUuid = UUID.randomUUID();
    Set<Identifier> missingIdentifiers =
        new HashSet<>(
            Set.of(
                Identifier.builder()
                    .uuid(identifierUuid)
                    .namespace("namespace")
                    .id("value")
                    .build()));
    identifiableToUpdate.setIdentifiers(missingIdentifiers);

    Identifier identifierToDelete = new Identifier(uuid, "other", "foo");
    Identifiable existingIdentifiable = new Identifiable();
    existingIdentifiable.setUuid(uuid);
    existingIdentifiable.setLabel(new LocalizedText(Locale.GERMAN, "Label"));
    existingIdentifiable.setIdentifiers(Set.of(identifierToDelete));

    Identifiable existingIdentifiableWithUpdatedIdentifiers = new Identifiable();
    existingIdentifiableWithUpdatedIdentifiers.setUuid(uuid);
    existingIdentifiableWithUpdatedIdentifiers.setLabel(new LocalizedText(Locale.GERMAN, "Label"));
    existingIdentifiableWithUpdatedIdentifiers.setIdentifiers(
        Set.of(
            Identifier.builder().uuid(identifierUuid).namespace("namespace").id("value").build()));

    when(repo.getByUuid(eq(existingIdentifiable.getUuid())))
        .thenReturn(existingIdentifiable)
        .thenReturn(existingIdentifiableWithUpdatedIdentifiers);
    when(identifierService.saveForIdentifiable(any(), any()))
        .thenReturn(
            Set.of(
                Identifier.builder()
                    .uuid(identifierUuid)
                    .namespace("namespace")
                    .id("value")
                    .build()));
    when(urlAliasService.getLocalizedUrlAliases(any(UUID.class))).thenReturn(null);

    service.update(identifiableToUpdate);

    assertThat(identifiableToUpdate.getIdentifiers()).hasSize(1);
    Identifier actualIdentifier = identifiableToUpdate.getIdentifiers().stream().findFirst().get();
    assertThat(actualIdentifier.getUuid()).isEqualTo(identifierUuid);
    assertThat(actualIdentifier.getNamespace()).isEqualTo("namespace");
    assertThat(actualIdentifier.getId()).isEqualTo("value");

    verify(identifierService, times(1))
        .saveForIdentifiable(any(UUID.class), argThat(set -> set.size() == 1));
    verify(identifierService, times(1)).delete(eq(Set.of(identifierToDelete)));
  }

  @DisplayName(
      "fills the provided identifiers with the missing values, obtained from the existing identifiers, where present")
  @Test
  void fillProvidedIdentifiers()
      throws CudamiServiceException, ValidationException, ServiceException {
    UUID uuid = UUID.randomUUID();
    UUID[] identifierUuids = new UUID[] {UUID.randomUUID(), UUID.randomUUID()};
    // The identifiable, which we want to update, carries one identifier, which is already
    // present in the database and another one, which is new
    Identifier existingIdentifier = new Identifier(uuid, "namespace1", "1");
    existingIdentifier.setUuid(identifierUuids[0]);

    Identifiable identifiableToUpdate = new Identifiable();
    identifiableToUpdate.setUuid(uuid);
    identifiableToUpdate.setLabel(new LocalizedText(Locale.GERMAN, "Label"));
    identifiableToUpdate.addIdentifier(existingIdentifier);
    identifiableToUpdate.addIdentifier(
        Identifier.builder().namespace("namespace2").id("1").uuid(identifierUuids[1]).build());

    // The existing identifiable carries one identifier
    Identifiable existingIdentifiable = new Identifiable();
    existingIdentifiable.setUuid(uuid);
    existingIdentifiable.setLabel(new LocalizedText(Locale.GERMAN, "Label"));
    existingIdentifiable.addIdentifier(existingIdentifier);

    when(repo.getByUuid(eq(existingIdentifiable.getUuid()))).thenReturn(existingIdentifiable);
    when(urlAliasService.getLocalizedUrlAliases(any(UUID.class))).thenReturn(null);
    when(identifierService.saveForIdentifiable(any(), any()))
        .thenReturn(
            Set.of(
                Identifier.builder()
                    .uuid(identifierUuids[1])
                    .namespace("namespace2")
                    .id("1")
                    .identifiable(uuid)
                    .build()));

    service.update(identifiableToUpdate);
    List<Identifier> actualIdentifiers = new ArrayList<>(identifiableToUpdate.getIdentifiers());
    // We sort the identifiers, for easier validation
    Collections.sort(
        actualIdentifiers,
        (i1, i2) -> {
          String key1 = i1.getNamespace() + ":" + i1.getId();
          String key2 = i2.getNamespace() + ":" + i2.getId();
          return key1.compareTo(key2);
        });
    assertThat(actualIdentifiers.get(0)).isEqualTo(existingIdentifier);
    assertThat(actualIdentifiers.get(1).getIdentifiable()).isEqualTo(uuid);
    assertThat(actualIdentifiers.get(1).getNamespace()).isEqualTo("namespace2");
    assertThat(actualIdentifiers.get(1).getId()).isEqualTo("1");

    // Only one identifier was saved - the identifier, was was provided, but did not already exist
    verify(identifierService, times(1))
        .saveForIdentifiable(any(UUID.class), argThat(set -> set.size() == 1));
    // No identifier was deleted at all
    verify(identifierService, never()).delete(any(Set.class));
  }

  @DisplayName("throws a ValidationException on save and update when the label is null")
  @Test
  public void nullLabelThrowsValidationException() {
    Manifestation manifestation = Manifestation.builder().build();

    assertThrows(
        ValidationException.class,
        () -> {
          service.update(manifestation);
        });

    assertThrows(
        ValidationException.class,
        () -> {
          service.save(manifestation);
        });
  }

  @DisplayName("throws a ValidationException on save and update when the label is empty")
  @Test
  public void emptyLabelThrowsValidationException() {
    Manifestation manifestation = Manifestation.builder().build();
    manifestation.setLabel(new LocalizedText());

    assertThrows(
        ValidationException.class,
        () -> {
          service.update(manifestation);
        });

    assertThrows(
        ValidationException.class,
        () -> {
          service.save(manifestation);
        });
  }

  @DisplayName(
      "does not generate duplicates for identical slugs for identical languages but different scripts")
  @Test
  public void avoidDuplicatesForDifferentScripts()
      throws ValidationException, CudamiServiceException, ServiceException {
    LocalizedText personName =
        LocalizedText.builder()
            .text(LOCALE_UND_LATN, "Yu ji shan ren")
            .text(LOCALE_UND_HANI, "玉几山人")
            .build();
    Person person = Person.builder().name(personName).label(personName).build();

    when(urlAliasService.generateSlug(any(), any(String.class), eq(null)))
        .thenReturn("yu-ji-shan-ren");

    service.save(person);

    LocalizedUrlAliases actualLocalizedUrlAliases = person.getLocalizedUrlAliases();
    List<UrlAlias> actualUrlAliases = actualLocalizedUrlAliases.flatten();

    assertThat(actualUrlAliases).hasSize(1);
    assertThat(actualUrlAliases.get(0).getTargetLanguage()).isEqualTo(Locale.forLanguageTag("und"));
    assertThat(actualUrlAliases.get(0).getSlug()).isEqualTo("yu-ji-shan-ren");
  }
}
