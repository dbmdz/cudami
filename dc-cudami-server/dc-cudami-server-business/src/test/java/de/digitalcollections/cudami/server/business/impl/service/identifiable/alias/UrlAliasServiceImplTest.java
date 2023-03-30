package de.digitalcollections.cudami.server.business.impl.service.identifiable.alias;

import static de.digitalcollections.model.time.TimestampHelper.truncatedToMicros;
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

import de.digitalcollections.commons.web.SlugGenerator;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("The UrlAliasService implementation")
class UrlAliasServiceImplTest extends AbstractServiceImplTest {

  private LocaleService localeService;

  private UrlAliasRepository repo;
  private UrlAliasServiceImpl service;

  private SlugGenerator slugGenerator;

  @BeforeEach
  public void beforeEach() throws Exception {
    super.beforeEach();
    repo = mock(UrlAliasRepository.class);
    localeService = mock(LocaleService.class);
    when(localeService.getDefaultLanguage()).thenReturn("en");
    when(localeService.getDefaultLocale()).thenReturn(Locale.ENGLISH);
    slugGenerator = mock(SlugGenerator.class);
    when(slugGenerator.generateSlug(any(String.class))).thenReturn("slug");
    service = new UrlAliasServiceImpl(repo, slugGenerator, localeService);
  }

  @DisplayName("can successfully validate an empty LocalizedUrlAlias")
  @Test
  public void allowEmptyLocalizedUrlAlias() throws ValidationException {
    service.validate(null);
    service.validate(new LocalizedUrlAliases());
  }

  @DisplayName(
      "can successfully validate a LocalizedUrlAlias with multiple UrlAliases for the same website,target,language tuple, if only one is primary")
  @Test
  public void allowOnePrimaryForTheSameTuple() throws ValidationException {
    UUID targetUuid = UUID.randomUUID();
    UUID websiteUuid = UUID.randomUUID();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid),
        createUrlAlias("hurz2", true, "de", false, targetUuid, websiteUuid));
    service.validate(localizedUrlAliases);
  }

  @DisplayName(
      "can successfully validate a LocalizedUrlAlias with multiple UrlAliases for the different website,target,language tuples, if only one of each is primary")
  @Test
  public void allowOnePrimaryPerTuple() throws ValidationException {
    UUID targetUuid = UUID.randomUUID();
    UUID websiteUuid1 = UUID.randomUUID();
    UUID websiteUuid2 = UUID.randomUUID();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid1),
        createUrlAlias("hurz2", true, "de", false, targetUuid, websiteUuid1),
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid2));
    service.validate(localizedUrlAliases);
  }

  @DisplayName("can successfully validate a LocalizedUrlAlias with one primary UrlAlias")
  @Test
  public void allowOnePrimaryUrlAlias() throws ValidationException {
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, UUID.randomUUID(), UUID.randomUUID()));
    service.validate(localizedUrlAliases);
  }

  @DisplayName("checkPublication does not override an existing publication date")
  @Test
  public void checkPublicationDoesNotOverridePublicationDate()
      throws ServiceException, RepositoryException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(true);
    LocalDateTime publicationDate = LocalDateTime.now();
    urlAlias.setLastPublished(publicationDate);

    when(repo.getByUuid(eq(urlAlias.getUuid()))).thenReturn(urlAlias);
    service.checkPublication(urlAlias);

    LocalDateTime expectedPublicationDate = truncatedToMicros(publicationDate);
    assertThat(urlAlias.getLastPublished()).isEqualTo(expectedPublicationDate);
  }

  @DisplayName("checkPublication does not set lastPublished primary is set to false")
  @Test
  public void checkPublicationLastPublishedNotSet() throws ServiceException, RepositoryException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(true);
    LocalDateTime publicationDate = LocalDateTime.now();
    urlAlias.setLastPublished(publicationDate);
    when(repo.getByUuid(eq(urlAlias.getUuid()))).thenReturn(urlAlias);

    UrlAlias changedUrlAlias = createDeepCopy(urlAlias);
    changedUrlAlias.setPrimary(false);

    LocalDateTime expectedPublicationDate = truncatedToMicros(publicationDate);
    assertThat(changedUrlAlias.getLastPublished()).isEqualTo(expectedPublicationDate);
    service.checkPublication(changedUrlAlias);
    assertThat(changedUrlAlias.getLastPublished()).isEqualTo(expectedPublicationDate);
    assertThat(changedUrlAlias.getLastPublished().compareTo(expectedPublicationDate))
        .isEqualTo(0); // =equal
    assertThat(changedUrlAlias.isPrimary()).isFalse();
  }

  @DisplayName("checkPublication sets lastPublished to now when an alias becomes primary again")
  @Test
  public void checkPublicationSetLastPublishedAgain() throws ServiceException, RepositoryException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(false);
    LocalDateTime publicationDate = LocalDateTime.now().minusDays(1);
    urlAlias.setLastPublished(publicationDate);
    when(repo.getByUuid(eq(urlAlias.getUuid()))).thenReturn(urlAlias);

    UrlAlias changedUrlAlias = createDeepCopy(urlAlias);
    changedUrlAlias.setPrimary(true);

    LocalDateTime expectedPublicationDate = truncatedToMicros(publicationDate);
    assertThat(changedUrlAlias.getLastPublished()).isEqualTo(expectedPublicationDate);
    service.checkPublication(changedUrlAlias);
    assertThat(changedUrlAlias.getLastPublished()).isNotEqualTo(expectedPublicationDate);
    assertThat(changedUrlAlias.getLastPublished().compareTo(expectedPublicationDate))
        .isEqualTo(1); // =later than publicationDate
    assertThat(changedUrlAlias.isPrimary()).isTrue();
  }

  @DisplayName("checkPublication sets date to now for primary, if unset")
  @Test
  public void checkPublicationSetsNowForPrimaryIfUnset() throws ServiceException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(true);
    urlAlias.setLastPublished(null);
    service.checkPublication(urlAlias);
    assertThat(urlAlias.getLastPublished()).isNotNull();
  }

  @DisplayName("checkPublication throws an exception when changing an already published urlalias")
  @Test
  public void checkPublicationThrowsExceptionForAlreadyPublished() throws RepositoryException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(true);
    LocalDateTime publicationDate = LocalDateTime.now();
    urlAlias.setLastPublished(publicationDate);
    when(repo.getByUuid(eq(urlAlias.getUuid()))).thenReturn(urlAlias);

    UrlAlias changedUrlAlias = createDeepCopy(urlAlias);
    changedUrlAlias.setSlug("foo");

    assertThrows(
        ServiceException.class,
        () -> {
          service.checkPublication(changedUrlAlias);
        });
  }

  // -------------------------------------------------------------------------
  private UrlAlias createUrlAlias(
      String slug,
      boolean setUuid,
      String language,
      boolean primary,
      UUID targetUuid,
      UUID websiteUuid) {
    UrlAlias urlAlias = new UrlAlias();
    if (setUuid) {
      urlAlias.setUuid(UUID.randomUUID());
    }
    urlAlias.setPrimary(primary);
    urlAlias.setTargetUuid(targetUuid);
    urlAlias.setSlug(slug);
    urlAlias.setTargetIdentifiableType(IdentifiableType.ENTITY);
    urlAlias.setTargetIdentifiableObjectType(IdentifiableObjectType.COLLECTION);
    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setCreated(LocalDateTime.now());
    urlAlias.setTargetLanguage(Locale.forLanguageTag(language));
    urlAlias.setWebsite(createWebsite(websiteUuid));
    return urlAlias;
  }

  private Website createWebsite(UUID uuid) {
    Website website = new Website();
    website.setUuid(uuid);
    String dummyUrl = "http://" + uuid + "/";
    try {
      website.setUrl(new URL(dummyUrl));
    } catch (MalformedURLException e) {
      throw new RuntimeException("Cannot create dummy URL=" + dummyUrl + ": " + e, e);
    }
    return website;
  }

  @DisplayName("deleteForTarget with force deletes everything")
  @Test
  public void deleteForTargetWithForce() throws ServiceException, RepositoryException {
    UUID targetUuid = UUID.randomUUID();
    LocalizedUrlAliases targetLocalizedUrlAliases = new LocalizedUrlAliases();
    targetLocalizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));

    when(repo.getByIdentifiable(eq(targetUuid))).thenReturn(targetLocalizedUrlAliases);
    when(repo.delete(any(List.class))).thenReturn(1);

    assertThat(service.deleteAllForTarget(targetUuid, true)).isTrue();

    ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(repo, times(1)).delete(listArgumentCaptor.capture());
    assertThat(listArgumentCaptor.getValue()).hasSize(1);
  }

  @DisplayName("deleteForTarget with unset uuid deletes nothing and returns false")
  @Test
  public void deleteForTargetWithUuidNull() throws ServiceException, RepositoryException {
    assertThat(service.deleteByIdentifiable(null, true)).isFalse();
    verify(repo, never()).deleteByUuid(any());
  }

  @DisplayName("returns false, when no single UrlAlias of a list could be deleted")
  @Test
  public void deleteNoUrlAliasesAtAll() throws ServiceException, RepositoryException {
    when(repo.delete(any(List.class))).thenReturn(0);

    assertThat(service.delete(List.of(UUID.randomUUID(), UUID.randomUUID()))).isFalse();
  }

  @DisplayName("returns false when trying to delete a nonexistant UrlAlias by its uuid")
  @Test
  public void deleteNonexistantSingleUrlAlias() throws ServiceException, RepositoryException {
    when(repo.getByUuid(any(UUID.class))).thenReturn(null);

    assertThat(service.deleteByUuid(UUID.randomUUID())).isFalse();
  }

  @DisplayName("returns true when an existant UrlAlias could be deleted")
  @Test
  public void deleteSingleUrlAlias() throws ServiceException, RepositoryException {
    when(repo.delete(any(List.class))).thenReturn(1);

    assertThat(service.deleteByUuid(UUID.randomUUID())).isTrue();
  }

  @DisplayName("returns true, when at least one UrlAlias of a list could be deleted")
  @Test
  public void deleteSomeUrlAliases() throws ServiceException, RepositoryException {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    when(repo.deleteByUuid(eq(List.of(uuid1, uuid2)))).thenReturn(1);

    assertThat(service.delete(List.of(uuid1, uuid2))).isTrue();
  }

  @DisplayName("filters according target language, when it exists")
  @Test
  public void displayFilterExistingTargetLanguage() {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()),
        createUrlAlias("foo", true, "en", false, UUID.randomUUID(), UUID.randomUUID()));

    LocalizedUrlAliases actual =
        service.filterForLocaleWithFallback(Locale.forLanguageTag("de"), expected);

    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("de")));
    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("en"))).isFalse();
  }

  @DisplayName(
      "filtering goes back to system wide fallback language, if no item found for target language")
  @Test
  public void fallbackToSystemLanguage() {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(
        createUrlAlias("hurzo", true, "it", false, UUID.randomUUID(), UUID.randomUUID()),
        createUrlAlias("foo", true, "en", false, UUID.randomUUID(), UUID.randomUUID()));

    LocalizedUrlAliases actual =
        service.filterForLocaleWithFallback(Locale.forLanguageTag("de"), expected);

    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("en")));
    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("it"))).isFalse();
  }

  @DisplayName("can filter an empty list of nonmatching languages for slug without error")
  @Test
  public void filterEmptyListOfNonmatchingSlugs() {
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases(List.of());
    assertThat(service.removeNonmatchingLanguagesForSlug(localizedUrlAliases, "test"))
        .isEqualTo(localizedUrlAliases);
  }

  @DisplayName("can filter nonmatching languages for a slug out of LocalizedUrlAliases")
  @Test
  public void filterNonmatchingLanguagesForSlug() {
    LocalizedUrlAliases localizedUrlAliases =
        new LocalizedUrlAliases(
            List.of(
                createUrlAlias("foo", true, "de", true, null, null),
                createUrlAlias("blubb", true, "de", false, null, null),
                createUrlAlias("bar", true, "en", true, null, null)));

    // It works for primary slugs
    LocalizedUrlAliases actual =
        service.removeNonmatchingLanguagesForSlug(localizedUrlAliases, "foo");
    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("de"))).isTrue();
    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("en"))).isFalse();

    // ... and it works for non primary slugs, too
    actual = service.removeNonmatchingLanguagesForSlug(localizedUrlAliases, "blubb");
    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("de"))).isTrue();
    assertThat(actual.hasTargetLanguage(Locale.forLanguageTag("en"))).isFalse();
  }

  @DisplayName("retrieve primary links of a target as List<UrlAlias>")
  @Test
  public void findPrimaryLinksForTarget() throws ServiceException, RepositoryException {
    UUID targetUuid = UUID.randomUUID();
    UrlAlias ua1 = createUrlAlias("slug1", true, "de", true, targetUuid, null);
    UrlAlias ua2 = createUrlAlias("slug2", true, "en", true, targetUuid, null);
    UrlAlias ua3 = createUrlAlias("npslug", true, "de", false, targetUuid, null);
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases(ua1, ua2, ua3);
    when(repo.getByIdentifiable(eq(targetUuid))).thenReturn(localizedUrlAliases);
    assertThat(service.getPrimaryUrlAliasesByIdentifiable(targetUuid))
        .containsAll(List.of(ua1, ua2));
  }

  @DisplayName("generates slugs with numeric suffix for default locale")
  @Test
  public void generateSlugWithNumericSuffixForDefaultLocale()
      throws ServiceException, RepositoryException {
    String expected = "label-2";
    UUID websiteUuid = UUID.randomUUID();

    when(slugGenerator.generateSlug(eq("label"))).thenReturn("label");
    when(repo.hasUrlAlias(eq("label"), eq(websiteUuid), any(Locale.class))).thenReturn(true);
    when(repo.hasUrlAlias(eq("label-1"), eq(websiteUuid), any(Locale.class))).thenReturn(true);
    when(repo.hasUrlAlias(eq("label-2"), eq(websiteUuid), any(Locale.class))).thenReturn(false);

    assertThat(service.generateSlug(Locale.GERMAN, "label", websiteUuid)).isEqualTo(expected);
  }

  @DisplayName("can generate a slug without suffix")
  @Test
  public void generateSlugWithoutSuffix() throws ServiceException, RepositoryException {
    String expected = "label";
    UUID websiteUuid = UUID.randomUUID();

    when(slugGenerator.generateSlug(eq("label"))).thenReturn("label");
    when(repo.hasUrlAlias(eq("label"), eq(websiteUuid), any(Locale.class))).thenReturn(false);

    assertThat(service.generateSlug(Locale.GERMAN, "label", websiteUuid)).isEqualTo(expected);
  }

  @DisplayName(
      "returns the generic primary link in all languages, when no website uuid is provided")
  @Test
  public void genericPrimaryLinkForNoWebsite() throws ServiceException, RepositoryException {
    LocalizedUrlAliases expectedLocalizedUrlAliases = new LocalizedUrlAliases();
    expectedLocalizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    expectedLocalizedUrlAliases.add(
        createUrlAlias("hurz", true, "en", false, UUID.randomUUID(), UUID.randomUUID()));
    when(repo.findPrimaryLinksForWebsite(eq(null), eq("hurz"), eq(false)))
        .thenReturn(expectedLocalizedUrlAliases);

    LocalizedUrlAliases actual = service.getPrimaryUrlAliases(null, "hurz", null);

    assertThat(actual).isEqualTo(expectedLocalizedUrlAliases);
  }

  @DisplayName(
      "returns the generic primary link in demanded language, when no website uuid is provided")
  @Test
  public void genericPrimaryLinkForNoWebsiteRestrictedtoLanguage()
      throws ServiceException, RepositoryException {

    UrlAlias germanUrlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    UrlAlias englishUrlAlias =
        createUrlAlias("hurz", true, "en", false, UUID.randomUUID(), UUID.randomUUID());

    LocalizedUrlAliases localizedUrlAliasesToPersist = new LocalizedUrlAliases();
    localizedUrlAliasesToPersist.add(germanUrlAlias);
    localizedUrlAliasesToPersist.add(englishUrlAlias);
    when(repo.findPrimaryLinksForWebsite(eq(null), eq("hurz"), eq(true)))
        .thenReturn(localizedUrlAliasesToPersist);

    LocalizedUrlAliases actual =
        service.getPrimaryUrlAliases(null, "hurz", Locale.forLanguageTag("de"));

    LocalizedUrlAliases expectedLocalizedUrlAliases = new LocalizedUrlAliases();
    expectedLocalizedUrlAliases.add(germanUrlAlias);
    assertThat(actual).isEqualTo(expectedLocalizedUrlAliases);
  }

  @DisplayName(
      "falls back to generic primary links when trying to get the primary links with a missing website uuid")
  @Test
  public void genericPrimaryLinksForPrimaryLinksWithMissingUuid()
      throws ServiceException, RepositoryException {
    UUID uuid = UUID.randomUUID();
    String slug = "hützligrütz";

    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    when(repo.findPrimaryLinksForWebsite(eq(uuid), eq(slug), eq(false)))
        .thenReturn(new LocalizedUrlAliases());
    when(repo.findPrimaryLinksForWebsite(eq(null), eq(slug), eq(false))).thenReturn(expected);

    assertThat(service.getPrimaryUrlAliases(uuid, slug, null)).isEqualTo(expected);
  }

  @DisplayName("does not filter anything when the LocalizedUrlAliases is empty")
  @Test
  public void noFilteringForEmptyLocalizedUrlAliases() {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    LocalizedUrlAliases actual =
        service.filterForLocaleWithFallback(Locale.forLanguageTag("de"), new LocalizedUrlAliases());
    assertThat(actual).isEqualTo(expected);
  }

  @DisplayName("does not filter anything when the locale to be filtered is null")
  @Test
  public void noFilteringForNullLocale() {
    LocalizedUrlAliases expected = mock(LocalizedUrlAliases.class);

    service.filterForLocaleWithFallback(null, expected);
    verify(expected, never()).forEach(any());
  }

  @DisplayName("does not filter anything when the LocalizedUrlAliases is null")
  @Test
  public void noFilteringForNullLocalizedUrlAliases() {
    LocalizedUrlAliases actual = service.filterForLocaleWithFallback(null, null);
    assertThat(actual).isNull();
  }

  @DisplayName("raises a ServiceException when the repository throws an exception")
  @Test
  public void raiseException() throws RepositoryException {
    when(repo.getByUuid(any(UUID.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        ServiceException.class,
        () -> {
          service.getByUuid(UUID.randomUUID());
        });
  }

  @DisplayName(
      "raises a ServiceException when trying to get the primary links with a missing or empty slug")
  @Test
  public void raiseExceptionForPrimaryLinksWithMissingOrEmptySlug() throws ServiceException {
    assertThrows(
        ServiceException.class,
        () -> {
          service.getPrimaryUrlAliases(UUID.randomUUID(), null, null);
        });
    assertThrows(
        ServiceException.class,
        () -> {
          service.getPrimaryUrlAliases(UUID.randomUUID(), "", null);
        });
  }

  @DisplayName("raises a ServiceException when deleting leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenDeleteLeadsToAnException() throws RepositoryException {
    doThrow(RepositoryException.class).when(repo).delete(any(List.class));

    assertThrows(
        ServiceException.class,
        () -> {
          service.delete(List.of(UUID.randomUUID()));
        });
  }

  @DisplayName(
      "raises a ServiceException when finding an UrlAlias leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenFindLeadsToAnException() throws RepositoryException {
    doThrow(NullPointerException.class).when(repo).find(any(PageRequest.class));

    assertThrows(
        ServiceException.class,
        () -> {
          service.find(new PageRequest());
        });
  }

  @DisplayName(
      "raises a ServiceException when retriving LocalizedUrlAliases leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenRetrievingLocalizedUrlAliasesLeadsToAnException()
      throws RepositoryException {
    doThrow(NullPointerException.class).when(repo).getByIdentifiable(any(UUID.class));

    assertThrows(
        ServiceException.class,
        () -> {
          service.getByIdentifiable(UUID.randomUUID());
        });
  }

  @DisplayName(
      "raises a ServiceException when retriving the primary links leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenRetrievingPrimaryLinksLeadsToAnException()
      throws RepositoryException {
    doThrow(NullPointerException.class)
        .when(repo)
        .findPrimaryLinksForWebsite(any(UUID.class), any(String.class));

    assertThrows(
        ServiceException.class,
        () -> {
          service.getPrimaryUrlAliases(UUID.randomUUID(), "hützligrütz", null);
        });
  }

  @DisplayName("raises a ServiceException when creating leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenSaveLeadsToAnException() throws RepositoryException {
    doThrow(NullPointerException.class).when(repo).save(any(UrlAlias.class));

    assertThrows(
        ServiceException.class,
        () -> {
          service.save(
              createUrlAlias(
                  "hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName("raises a ServiceException when trying to create an empty UrlAlias")
  @Test
  public void raiseExceptionWhenSaveWithNullUrlAlias() throws ServiceException {
    assertThrows(
        ServiceException.class,
        () -> {
          service.save(null);
        });
  }

  @DisplayName("raises a ServiceException when trying to create an UrlAlias with existing UUID")
  @Test
  public void raiseExceptionWhenSaveWithUuid() throws RepositoryException {
    when(repo.getByUuid(any(UUID.class))).thenReturn(null);
    assertThrows(
        ServiceException.class,
        () -> {
          service.save(
              createUrlAlias(
                  "hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName("raises a ServiceException when updating leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenUpdateLeadsToAnException() throws RepositoryException {
    doThrow(NullPointerException.class).when(repo).getByUuid(any(UUID.class));

    assertThrows(
        ServiceException.class,
        () -> {
          service.update(
              createUrlAlias(
                  "hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName(
      "raises a ServiceException when updating leads to an exception in the repository at persisting")
  @Test
  public void raiseExceptionWhenUpdateLeadsToAnExceptionAtPersisting() throws RepositoryException {
    UrlAlias expected =
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    expected.setLastPublished(null);

    when(repo.getByUuid(any(UUID.class))).thenReturn(expected);
    doThrow(NullPointerException.class).when(repo).update(any());

    assertThrows(
        ServiceException.class,
        () -> {
          service.update(expected);
        });
  }

  @DisplayName("raises a ServiceException when trying to update an UrlAlias with missing UUID")
  @Test
  public void raiseExceptionWhenUpdateWithMissingUuid() throws ServiceException {
    assertThrows(
        ServiceException.class,
        () -> {
          service.update(
              createUrlAlias(
                  "hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName("raises a ServiceException when trying to update an empty UrlAlias")
  @Test
  public void raiseExceptionWhenUpdateWithNullUrlAlias() throws ServiceException {
    assertThrows(
        ServiceException.class,
        () -> {
          service.update(null);
        });
  }

  @DisplayName("returns an UrlAlias")
  @Test
  public void readExisting() throws ServiceException, RepositoryException {
    UrlAlias expected =
        createUrlAlias("hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID());

    when(repo.getByUuid(any(UUID.class))).thenReturn(expected);

    assertThat(service.getByUuid(UUID.randomUUID())).isEqualTo(expected);
  }

  @DisplayName("returns null, when an nonexisting UrlAlias should be retrieved")
  @Test
  public void readNonexisting() throws ServiceException, RepositoryException {
    when(repo.getByUuid(any(UUID.class))).thenReturn(null);

    assertThat(service.getByUuid(UUID.randomUUID())).isNull();
  }

  @DisplayName("returns null, an UrlAlias with uuid=null should be retrieved")
  @Test
  public void readNull() throws ServiceException, RepositoryException {
    when(repo.getByUuid(eq(null))).thenReturn(null);

    assertThat(service.getByUuid(null)).isNull();
  }

  @DisplayName("throws an exception when validating a LocalizedUrlAlias with no primary UrlAlias")
  @Test
  public void rejectNoPrimaryUrlAlias() throws ValidationException {
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    assertThrows(
        ValidationException.class,
        () -> {
          service.validate(localizedUrlAliases);
        });
  }

  @DisplayName(
      "throws an exception when validating a LocalizedUrlAlias with two non primary UrlAliases for the same slug, website,target and language tuple")
  @Test
  public void rejectTwoNonPrimaryUrlAliasesForTheSameTupleAndSlug() {
    UUID targetUuid = UUID.randomUUID();
    UUID websiteUuid = UUID.randomUUID();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", false, targetUuid, websiteUuid),
        createUrlAlias("hurz", true, "de", false, targetUuid, websiteUuid));
    assertThrows(
        ValidationException.class,
        () -> {
          service.validate(localizedUrlAliases);
        });
  }

  @DisplayName(
      "throws an exception when validating a LocalizedUrlAlias with two primary UrlAliases for the same website, target and language tuple")
  @Test
  public void rejectTwoPrimaryUrlAliasesForTheSameTuple() {
    UUID targetUuid = UUID.randomUUID();
    UUID websiteUuid = UUID.randomUUID();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid),
        createUrlAlias("hurz2", true, "de", true, targetUuid, websiteUuid));
    assertThrows(
        ValidationException.class,
        () -> {
          service.validate(localizedUrlAliases);
        });
  }

  @DisplayName(
      "throws an exception when validating a LocalizedUrlAlias with two primary UrlAliases for the same slug, website,target and language tuple")
  @Test
  public void rejectTwoPrimaryUrlAliasesForTheSameTupleAndSlug() {
    UUID targetUuid = UUID.randomUUID();
    UUID websiteUuid = UUID.randomUUID();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid),
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid));
    assertThrows(
        ValidationException.class,
        () -> {
          service.validate(localizedUrlAliases);
        });
  }

  @DisplayName(
      "throws an exception when validating a LocalizedUrlAlias with two primary UrlAliases for the same website, target and language tuple and one additional tuple")
  @Test
  public void rejectTwoPrimaryUrlAliasesForTheSameTuplePlusAdditional() {
    UUID targetUuid = UUID.randomUUID();
    UUID websiteUuid = UUID.randomUUID();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid),
        createUrlAlias("hurz2", true, "de", true, targetUuid, websiteUuid),
        createUrlAlias("hurz-en", true, "en", true, targetUuid, websiteUuid));
    assertThrows(
        ValidationException.class,
        () -> {
          service.validate(localizedUrlAliases);
        });
  }

  @DisplayName(
      "throws an exception when validating a LocalizedUrlAlias with one primary and one non primary UrlAlias for the same slug,website,target and language tuple")
  @Test
  public void rejectTwoUrlAliasesForTheSameTuple() {
    UUID targetUuid = UUID.randomUUID();
    UUID websiteUuid = UUID.randomUUID();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, targetUuid, websiteUuid),
        createUrlAlias("hurz", true, "de", false, targetUuid, websiteUuid));
    assertThrows(
        ValidationException.class,
        () -> {
          service.validate(localizedUrlAliases);
        });
  }

  @DisplayName("returns LocalizedUrlAliases for an UUID of an identifiable")
  @Test
  public void returnLocalizedUrlAliases() throws ServiceException, RepositoryException {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
    urlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    expected.add(urlAlias);

    when(repo.getByIdentifiable(any(UUID.class))).thenReturn(expected);

    assertThat(service.getByIdentifiable(UUID.randomUUID())).isEqualTo(expected);
  }

  @DisplayName("can return primary links")
  @Test
  public void returnPrimaryLinks() throws ServiceException, RepositoryException {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    when(repo.findPrimaryLinksForWebsite(any(UUID.class), any(String.class), eq(false)))
        .thenReturn(expected);

    assertThat(service.getPrimaryUrlAliases(UUID.randomUUID(), "hützligrütz", null))
        .isEqualTo(expected);
  }

  @DisplayName("can return generic primary links")
  @Test
  public void returnPrimaryLinksWithoutWebsite() throws ServiceException, RepositoryException {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    when(repo.findPrimaryLinksForWebsite(eq(null), any(String.class), eq(false)))
        .thenReturn(expected);

    assertThat(service.getPrimaryUrlAliases(null, "hützligrütz", null)).isEqualTo(expected);
  }

  @DisplayName("can return a SearchPageResult")
  @Test
  public void returnSearchPageResult() throws RepositoryException, ServiceException {
    PageResponse<LocalizedUrlAliases> expected = new PageResponse<>();
    LocalizedUrlAliases localizedUrlAlias = new LocalizedUrlAliases();
    localizedUrlAlias.add(
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    expected.setContent(List.of(localizedUrlAlias));

    when(repo.find(any(PageRequest.class))).thenReturn(expected);

    PageRequest pageRequest = new PageRequest();
    assertThat(service.find(pageRequest)).isEqualTo(expected);
  }

  @DisplayName("creates and saves an UrlAlias and returns it with set UUID")
  @Test
  public void saveUrlAlias() throws ServiceException {
    UrlAlias urlAlias =
        createUrlAlias("hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID());
    UrlAlias expected = createDeepCopy(urlAlias);
    expected.setUuid(UUID.randomUUID());

    UrlAlias expectedForSave = createDeepCopy(expected);
    service.save(urlAlias);
    // We have to set the UUID field in the expectedForSave UrlAlias for later comparison
    expectedForSave.setUuid(urlAlias.getUuid());

    assertThat(urlAlias).usingRecursiveComparison().isEqualTo(expectedForSave);
  }

  @DisplayName("throws an exception, when the query for existance of a slug leads to an exception")
  @Test
  public void throwsExceptionWhenSlugQueryFails() throws RepositoryException {
    when(repo.hasUrlAlias(any(String.class), any(UUID.class), any(Locale.class)))
        .thenThrow(new RepositoryException("foo"));

    assertThrows(
        ServiceException.class,
        () -> {
          service.generateSlug(Locale.GERMAN, "label", UUID.randomUUID());
        });
  }

  @DisplayName(
      "throws an exception, when the query for existance of a suffixed slug leads to an exception")
  @Test
  public void throwsExceptionWhenSlugQueryForSuffixesFails() throws RepositoryException {
    when(slugGenerator.generateSlug(eq("label"))).thenReturn("label");
    when(repo.hasUrlAlias(eq("label"), any(UUID.class), any(Locale.class))).thenReturn(true);

    when(repo.hasUrlAlias(eq("label-1"), any(UUID.class), any(Locale.class)))
        .thenThrow(new RepositoryException("foo"));

    assertThrows(
        ServiceException.class,
        () -> {
          service.generateSlug(Locale.GERMAN, "label", UUID.randomUUID());
        });
  }

  @DisplayName("updates and returns an UrlAlias")
  @Test
  public void updateUrlAlias() throws RepositoryException, ServiceException {
    UrlAlias expected =
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());

    when(repo.getByUuid(any(UUID.class))).thenReturn(expected);

    UrlAlias expectedForUpdate = createDeepCopy(expected);

    service.update(expected);

    assertThat(expected).isEqualTo(expectedForUpdate);
  }
}
