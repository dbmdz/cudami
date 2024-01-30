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
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractUniqueObjectServiceImplTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class IdentifiableServiceImplTest extends AbstractUniqueObjectServiceImplTest {

  protected static final Locale LOCALE_UND_HANI =
      new Locale.Builder().setLanguage("und").setScript("Hani").build();
  protected static final Locale LOCALE_UND_LATN =
      new Locale.Builder().setLanguage("und").setScript("Latn").build();

  private IdentifierService identifierService;
  private IdentifiableRepository repo;
  private IdentifiableServiceImpl service;
  private UrlAliasService urlAliasService;

  @DisplayName("can add related entities by delegating it to the repository")
  @Test
  public void addRelatedEntity() throws Exception {
    service.addRelatedEntity(createIdentifiable(), createEntity());
    verify(repo, times(1)).addRelatedEntity(any(Identifiable.class), any(Entity.class));
  }

  @DisplayName("can add related fileresources by delegating it to the repository")
  @Test
  public void addRelatedFileResources() throws Exception {
    service.addRelatedFileresource(createIdentifiable(), createFileResource());
    verify(repo, times(1)).addRelatedFileresource(any(Identifiable.class), any(FileResource.class));
  }

  @DisplayName("allows two primary entries for different (website,target,language) tuples")
  @Test
  public void allowMultiplePrimariesForDifferentTuples() throws Exception {
    Identifiable target = createIdentifiable();
    Website website = createWebsite();

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setWebsite(website);
    firstPrimaryUrlAlias.setTarget(target);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setWebsite(website);
    secondPrimaryUrlAlias.setTarget(target);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("en"));
    secondPrimaryUrlAlias.setSlug("slug2");

    localizedUrlAliases.add(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(target.getUuid());
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);

    LocalizedText label = new LocalizedText(Locale.forLanguageTag("de"), "label");
    label.setText(Locale.forLanguageTag("en"), "label");
    identifiable.setLabel(label);
    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(target.getUuid());
    identifiableInDb.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "label"));

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

    service.update(identifiable);
  }

  @DisplayName(
      "does not generate duplicates for identical slugs for identical languages but different scripts")
  @Test
  public void avoidDuplicatesForDifferentScripts() throws Exception {
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

  @Override
  @BeforeEach
  public void beforeEach() throws Exception {
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

  @DisplayName("creates missing UrlAliases on an identifiable, when updating it")
  @Test
  public void createMissingUrlAliasesOnUpdate() throws Exception {
    UUID targetUuid = UUID.randomUUID();
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(targetUuid);
    identifiable.setLabel(new LocalizedText(Locale.GERMAN, "label"));

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    identifiableInDb.setLabel(new LocalizedText(Locale.GERMAN, "oldlabel"));

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

    service.update(identifiable);

    verify(urlAliasService, times(1)).save(any(UrlAlias.class), eq(true));
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
      "throws an exception, when two primary entries for the same (website,target,language) tuple are set")
  @Test
  public void exceptionOnMultiplePrimaryEntries() throws Exception {
    Identifiable target = createIdentifiable();
    UUID targetUuid = target.getUuid();

    Website website = new Website();
    website.setUuid(UUID.randomUUID());

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setWebsite(website);
    firstPrimaryUrlAlias.setTarget(target);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setWebsite(website);
    secondPrimaryUrlAlias.setTarget(target);
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

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

    when(urlAliasService.getPrimaryUrlAliasesByIdentifiable(any())).thenReturn(new ArrayList<>());
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
  public void exceptionOnMultiplePrimaryEntriesBasedOnSlug() throws Exception {
    Identifiable target = createIdentifiable();
    UUID targetUuid = target.getUuid();

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setWebsite(null);
    firstPrimaryUrlAlias.setTarget(target);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setWebsite(null);
    secondPrimaryUrlAlias.setTarget(target);
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

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

    doThrow(new ValidationException("no way!"))
        .when(urlAliasService)
        .validate(eq(localizedUrlAliases));

    assertThrows(
        ValidationException.class,
        () -> {
          service.update(identifiable);
        });
  }

  @DisplayName("throws an Exception to trigger a rollback on save, when saving in the repo fails")
  @Test
  public void exceptionOnSaveWhenRepoFails() throws RepositoryException, ValidationException {
    doThrow(RepositoryException.class).when(repo).save(any(Identifiable.class));

    Identifiable identifiable = createIdentifiable();
    identifiable.setLabel("label");

    assertThrows(
        ServiceException.class,
        () -> {
          service.save(identifiable);
        });
  }

  @DisplayName(
      "throws an Exception to trigger a rollback on save, when creating and saving an UrlAlias fails")
  @Test
  public void exceptionOnSaveWhenSavingUrlAliasFails() throws Exception {
    doThrow(ServiceException.class).when(urlAliasService).save(any(UrlAlias.class));

    Identifiable identifiable = new Identifiable();
    identifiable.setLabel("label");

    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setPrimary(true);
    urlAlias.setSlug("label");

    assertThrows(
        ServiceException.class,
        () -> {
          service.save(identifiable);
        });
  }

  @DisplayName(
      "throws an Exception to trigger a rollback on update, when updating in the repo fails")
  @Test
  public void exceptionOnUpdateWhenRepoFails() throws RepositoryException, ValidationException {
    doThrow(NullPointerException.class).when(repo).update(any(Identifiable.class));

    Identifiable identifiable = Identifiable.builder().label("label").build();
    assertThrows(
        ServiceException.class,
        () -> {
          service.update(identifiable);
        });
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

  @DisplayName("can save Identifiables without UrlAliases and creates an UrlAlias for them")
  @Test
  public void saveIdentifiableWithoutUrlAliases() throws Exception {
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

  @DisplayName("updates existing UrlAliases on an identifiable, when updating it")
  @Test
  public void updateExistingUrlAliasesOnUpdate() throws Exception {
    Identifiable target = createIdentifiable();
    UUID targetUuid = target.getUuid();

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
    urlAlias.setTarget(target);
    localizedUrlAliases.add(urlAlias);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);
    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiable);

    service.update(identifiable);

    verify(urlAliasService, times(1)).update(eq(urlAlias));
  }

  @DisplayName("Filters out non-provided identifiers on update")
  @Test
  void updateRemovedNonProvidedIdentifiers() throws Exception {
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

    Identifier identifierToDelete = Identifier.builder().namespace("other").id("foo").build();
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

    when(repo.getByExample(any(Identifiable.class)))
        .thenReturn(existingIdentifiable)
        .thenReturn(existingIdentifiableWithUpdatedIdentifiers);
    when(urlAliasService.getByIdentifiable(any(Identifiable.class))).thenReturn(null);

    service.update(identifiableToUpdate);

    assertThat(identifiableToUpdate.getIdentifiers()).hasSize(1);
    Identifier actualIdentifier = identifiableToUpdate.getIdentifiers().stream().findFirst().get();
    assertThat(actualIdentifier.getUuid()).isEqualTo(identifierUuid);
    assertThat(actualIdentifier.getNamespace()).isEqualTo("namespace");
    assertThat(actualIdentifier.getId()).isEqualTo("value");
  }

  @DisplayName("update Identifiable with new language and primary UrlAlias")
  @Test
  public void updateWithAdditionalLanguage() throws Exception {
    Identifiable identifiableToUpdate = createIdentifiable();
    UUID targetUuid = identifiableToUpdate.getUuid();

    // in DB
    UrlAlias firstStoredPrimaryUrlAlias = new UrlAlias();
    firstStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setPrimary(true);
    firstStoredPrimaryUrlAlias.setTarget(identifiableToUpdate);
    firstStoredPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstStoredPrimaryUrlAlias.setSlug("slug1");

    LocalizedUrlAliases storedUrlAliases = new LocalizedUrlAliases(firstStoredPrimaryUrlAlias);

    when(urlAliasService.getByIdentifiable(eq(identifiableToUpdate))).thenReturn(storedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText labelDb = new LocalizedText(Locale.GERMAN, "label");
    identifiableInDb.setLabel(labelDb);
    identifiableInDb.setLocalizedUrlAliases(storedUrlAliases);

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

    // aliases in object to update
    UrlAlias firstPrimaryUrlAlias = new UrlAlias(); // equals to DB
    firstPrimaryUrlAlias.setUuid(firstStoredPrimaryUrlAlias.getUuid());
    firstPrimaryUrlAlias.setCreated(firstStoredPrimaryUrlAlias.getCreated());
    firstPrimaryUrlAlias.setLastPublished(firstStoredPrimaryUrlAlias.getLastPublished());
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTarget(identifiableInDb);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setTarget(identifiableInDb);
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

  @DisplayName("update Identifiable with only one different primary UrlAlias")
  @Test
  public void updateWithOnePrimaryUrlAliasOnly() throws Exception {
    Identifiable identifiableToUpdate = createIdentifiable();
    UUID targetUuid = identifiableToUpdate.getUuid();

    // in DB
    LocalizedUrlAliases storedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstStoredPrimaryUrlAlias = new UrlAlias();
    firstStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setPrimary(true);
    firstStoredPrimaryUrlAlias.setTarget(identifiableToUpdate);
    firstStoredPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstStoredPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondStoredPrimaryUrlAlias = new UrlAlias();
    secondStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setPrimary(true);
    secondStoredPrimaryUrlAlias.setTarget(identifiableToUpdate);
    secondStoredPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondStoredPrimaryUrlAlias.setSlug("slug2");

    storedUrlAliases.add(firstStoredPrimaryUrlAlias, secondStoredPrimaryUrlAlias);

    when(urlAliasService.getByIdentifiable(eq(identifiableToUpdate))).thenReturn(storedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiableInDb.setLabel(label);
    identifiableInDb.setLocalizedUrlAliases(storedUrlAliases);

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

    // new one
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTarget(identifiableInDb);
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

  @DisplayName("update Identifiable w/o localizedUrlAliases")
  @Test
  public void updateWithoutUrlAliases() throws Exception {
    Identifiable identifiableToUpdate = createIdentifiable();
    UUID targetUuid = identifiableToUpdate.getUuid();

    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTarget(identifiableToUpdate);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondPrimaryUrlAlias.setCreated(LocalDateTime.now());
    secondPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setTarget(identifiableToUpdate);
    secondPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondPrimaryUrlAlias.setSlug("slug2");

    localizedUrlAliases.add(firstPrimaryUrlAlias, secondPrimaryUrlAlias);

    when(urlAliasService.getByIdentifiable(eq(identifiableToUpdate)))
        .thenReturn(localizedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText labelDb = new LocalizedText(Locale.GERMAN, "label");
    labelDb.setText(Locale.ENGLISH, "label");
    identifiableInDb.setLabel(labelDb);
    identifiableInDb.setLocalizedUrlAliases(localizedUrlAliases);

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

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
  public void updateWithPrimaryUrlAliasesOnly() throws Exception {
    Identifiable identifiableToUpdate = createIdentifiable();
    UUID targetUuid = identifiableToUpdate.getUuid();

    // in DB (should be unset)
    LocalizedUrlAliases storedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstStoredPrimaryUrlAlias = new UrlAlias();
    firstStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    firstStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    firstStoredPrimaryUrlAlias.setPrimary(true);
    firstStoredPrimaryUrlAlias.setTarget(identifiableToUpdate);
    firstStoredPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstStoredPrimaryUrlAlias.setSlug("slug1");

    UrlAlias secondStoredPrimaryUrlAlias = new UrlAlias();
    secondStoredPrimaryUrlAlias.setUuid(UUID.randomUUID());
    secondStoredPrimaryUrlAlias.setCreated(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setLastPublished(LocalDateTime.now());
    secondStoredPrimaryUrlAlias.setPrimary(true);
    secondStoredPrimaryUrlAlias.setTarget(identifiableToUpdate);
    secondStoredPrimaryUrlAlias.setTargetLanguage(Locale.ENGLISH);
    secondStoredPrimaryUrlAlias.setSlug("slug2");

    storedUrlAliases.add(firstStoredPrimaryUrlAlias, secondStoredPrimaryUrlAlias);

    when(urlAliasService.getByIdentifiable(eq(identifiableToUpdate))).thenReturn(storedUrlAliases);

    Identifiable identifiableInDb = new Identifiable();
    identifiableInDb.setUuid(targetUuid);
    LocalizedText label = new LocalizedText(Locale.GERMAN, "label");
    label.setText(Locale.ENGLISH, "label");
    identifiableInDb.setLabel(label);
    identifiableInDb.setLocalizedUrlAliases(storedUrlAliases);

    when(repo.getByExample(any(Identifiable.class))).thenReturn(identifiableInDb);

    // new ones
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

    UrlAlias firstPrimaryUrlAlias = new UrlAlias();
    firstPrimaryUrlAlias.setPrimary(true);
    firstPrimaryUrlAlias.setTarget(identifiableInDb);
    firstPrimaryUrlAlias.setTargetLanguage(Locale.GERMAN);
    firstPrimaryUrlAlias.setSlug("slug-de-new");

    UrlAlias secondPrimaryUrlAlias = new UrlAlias();
    secondPrimaryUrlAlias.setPrimary(true);
    secondPrimaryUrlAlias.setTarget(identifiableInDb);
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

  @DisplayName("returns null when a single queried returns no result")
  @Test
  public void returnNullForNonexistingGetByExample() throws RepositoryException, ServiceException {
    Identifiable example = Identifiable.builder().uuid(UUID.randomUUID()).build();
    when(repo.getByExample(eq(example))).thenReturn(null);

    assertThat(service.getByExample(example)).isNull();
  }

  @DisplayName("can return a single Identifiable by example")
  @Test
  public void returnIdentifiableOnGetByExample() throws RepositoryException, ServiceException {
    Identifiable example = Identifiable.builder().uuid(UUID.randomUUID()).build();
    when(repo.getByExamples(eq(List.of(example)))).thenReturn(List.of(example));

    assertThat(service.getByExample(example)).isEqualTo(example);
  }

  @DisplayName("can return a partial result of multiple Identifiables by example")
  @Test
  public void returnMultipleIdentifiablesByExample() throws RepositoryException, ServiceException {
    Identifiable example1 = Identifiable.builder().uuid(UUID.randomUUID()).build();
    Identifiable example2 = Identifiable.builder().uuid(UUID.randomUUID()).build();
    Identifiable example3 = Identifiable.builder().uuid(UUID.randomUUID()).build();

    when(repo.getByExamples(eq(List.of(example1, example2, example3))))
        .thenReturn(List.of(example1, example2));

    assertThat(service.getByExamples(List.of(example1, example2, example3)))
        .containsExactlyInAnyOrder(example1, example2);
  }
}
