package de.digitalcollections.cudami.server.business.impl.service.identifiable.alias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.commons.web.SlugGenerator;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
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
class UrlAliasServiceImplTest {

  private LocaleService localeService;

  private UrlAliasServiceImpl service;

  private UrlAliasRepository repo;

  private SlugGenerator slugGenerator;

  @BeforeEach
  public void beforeEach() {
    repo = mock(UrlAliasRepository.class);
    localeService = mock(LocaleService.class);
    when(localeService.getDefaultLanguage()).thenReturn("en");
    when(localeService.getDefaultLocale()).thenReturn(Locale.ENGLISH);
    slugGenerator = mock(SlugGenerator.class);
    when(slugGenerator.generateSlug(any(String.class))).thenReturn("slug");
    service = new UrlAliasServiceImpl(repo, slugGenerator, localeService);
  }

  @DisplayName("returns null, when an nonexisting UrlAlias should be retrieved")
  @Test
  public void readNonexisting() throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.findOne(any(UUID.class))).thenReturn(null);

    assertThat(service.findOne(UUID.randomUUID())).isNull();
  }

  @DisplayName("returns null, an UrlAlias with uuid=null should be retrieved")
  @Test
  public void readNull() throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.findOne(eq(null))).thenReturn(null);

    assertThat(service.findOne(null)).isNull();
  }

  @DisplayName("raises a ServiceException when the repository throws an exception")
  @Test
  public void raiseException() throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.findOne(any(UUID.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findOne(UUID.randomUUID());
        });
  }

  @DisplayName("returns an UrlAlias")
  @Test
  public void readExisting() throws CudamiServiceException, UrlAliasRepositoryException {
    UrlAlias expected =
        createUrlAlias("hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID());

    when(repo.findOne(any(UUID.class))).thenReturn(expected);

    assertThat(service.findOne(UUID.randomUUID())).isEqualTo(expected);
  }

  @DisplayName("raises a ServiceException when trying to create an empty UrlAlias")
  @Test
  public void raiseExceptionWhenSaveWithNullUrlAlias() throws CudamiServiceException {
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.create(null);
        });
  }

  @DisplayName("raises a ServiceException when trying to create an UrlAlias with existing UUID")
  @Test
  public void raiseExceptionWhenSaveWithUuid() throws UrlAliasRepositoryException {
    when(repo.findOne(any(UUID.class))).thenReturn(null);
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.create(
              createUrlAlias(
                  "hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName("raises a ServiceException when creating leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenSaveLeadsToAnException()
      throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.save(any(UrlAlias.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.create(
              createUrlAlias(
                  "hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName("creates and saves an UrlAlias and returns it with set UUID")
  @Test
  public void saveUrlAlias() throws CudamiServiceException, UrlAliasRepositoryException {
    UrlAlias urlAlias =
        createUrlAlias("hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID());
    UrlAlias expected = deepCopy(urlAlias);
    expected.setUuid(UUID.randomUUID());

    when(repo.save(eq(urlAlias))).thenReturn(expected);

    assertThat(service.create(urlAlias)).isEqualTo(expected);
  }

  @DisplayName("raises a ServiceException when trying to update an empty UrlAlias")
  @Test
  public void raiseExceptionWhenUpdateWithNullUrlAlias() throws CudamiServiceException {
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.update(null);
        });
  }

  @DisplayName("raises a ServiceException when trying to update an UrlAlias with missing UUID")
  @Test
  public void raiseExceptionWhenUpdateWithMissingUuid() throws CudamiServiceException {
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.update(
              createUrlAlias(
                  "hützligrütz", false, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName("raises a ServiceException when updating leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenUpdateLeadsToAnException()
      throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.findOne(any(UUID.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.update(
              createUrlAlias(
                  "hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
        });
  }

  @DisplayName(
      "raises a ServiceException when updating leads to an exception in the repository at persisting")
  @Test
  public void raiseExceptionWhenUpdateLeadsToAnExceptionAtPersisting()
      throws CudamiServiceException, UrlAliasRepositoryException {
    UrlAlias expected =
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    expected.setLastPublished(null);

    when(repo.findOne(any(UUID.class))).thenReturn(expected);
    when(repo.update(any())).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.update(expected);
        });
  }

  @DisplayName("updates and returns an UrlAlias")
  @Test
  public void updateUrlAlias() throws CudamiServiceException, UrlAliasRepositoryException {
    UrlAlias expected =
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());

    when(repo.findOne(any(UUID.class))).thenReturn(expected);
    when(repo.update(eq(expected))).thenReturn(expected);

    assertThat(service.update(expected)).isEqualTo(expected);
  }

  @DisplayName("raises a ServiceException when deleting leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenDeleteLeadsToAnException()
      throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.delete(any(List.class))).thenThrow(new UrlAliasRepositoryException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.delete(List.of(UUID.randomUUID()));
        });
  }

  @DisplayName("returns false when trying to delete a nonexistant UrlAlias by its uuid")
  @Test
  public void deleteNonexistantSingleUrlAlias()
      throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.findOne(any(UUID.class))).thenReturn(null);

    assertThat(service.delete(UUID.randomUUID())).isFalse();
  }

  @DisplayName("returns true when an existant UrlAlias could be deleted")
  @Test
  public void deleteSingleUrlAlias() throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.delete(any(List.class))).thenReturn(1);

    assertThat(service.delete(UUID.randomUUID())).isTrue();
  }

  @DisplayName("returns false, when no single UrlAlias of a list could be deleted")
  @Test
  public void deleteNoUrlAliasesAtAll() throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.delete(any(List.class))).thenReturn(0);

    assertThat(service.delete(List.of(UUID.randomUUID(), UUID.randomUUID()))).isFalse();
  }

  @DisplayName("returns true, when at least one UrlAlias of a list could be deleted")
  @Test
  public void deleteSomeUrlAliases() throws CudamiServiceException, UrlAliasRepositoryException {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    when(repo.delete(eq(List.of(uuid1, uuid2)))).thenReturn(1);

    assertThat(service.delete(List.of(uuid1, uuid2))).isTrue();
  }

  @DisplayName(
      "raises a ServiceException when retriving LocalizedUrlAliases leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenRetrievingLocalizedUrlAliasesLeadsToAnException()
      throws UrlAliasRepositoryException {
    when(repo.findAllForTarget(any(UUID.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findLocalizedUrlAliases(UUID.randomUUID());
        });
  }

  @DisplayName("returns LocalizedUrlAliases for an UUID of an identifiable")
  @Test
  public void returnLocalizedUrlAliases()
      throws CudamiServiceException, UrlAliasRepositoryException {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
    urlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    expected.add(urlAlias);

    when(repo.findAllForTarget(any(UUID.class))).thenReturn(expected);

    assertThat(service.findLocalizedUrlAliases(UUID.randomUUID())).isEqualTo(expected);
  }

  @DisplayName(
      "falls back to generic primary links when trying to get the primary links with a missing website uuid")
  @Test
  public void genericPrimaryLinksForPrimaryLinksWithMissingUuid()
      throws CudamiServiceException, UrlAliasRepositoryException {
    UUID uuid = UUID.randomUUID();
    String slug = "hützligrütz";

    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    when(repo.findPrimaryLinksForWebsite(eq(uuid), eq(slug))).thenReturn(new LocalizedUrlAliases());
    when(repo.findPrimaryLinksForWebsite(eq(null), eq(slug))).thenReturn(expected);

    assertThat(service.findPrimaryLinks(uuid, slug, null)).isEqualTo(expected);
  }

  @DisplayName(
      "raises a ServiceException when trying to get the primary links with a missing or empty slug")
  @Test
  public void raiseExceptionForPrimaryLinksWithMissingOrEmptySlug() throws CudamiServiceException {
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findPrimaryLinks(UUID.randomUUID(), null, null);
        });
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findPrimaryLinks(UUID.randomUUID(), "", null);
        });
  }

  @DisplayName(
      "raises a ServiceException when retriving the primary links leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenRetrievingPrimaryLinksLeadsToAnException()
      throws UrlAliasRepositoryException {
    when(repo.findPrimaryLinksForWebsite(any(UUID.class), any(String.class)))
        .thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findPrimaryLinks(UUID.randomUUID(), "hützligrütz", null);
        });
  }

  @DisplayName("can return primary links")
  @Test
  public void returnPrimaryLinks() throws CudamiServiceException, UrlAliasRepositoryException {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    when(repo.findPrimaryLinksForWebsite(any(UUID.class), any(String.class))).thenReturn(expected);

    assertThat(service.findPrimaryLinks(UUID.randomUUID(), "hützligrütz", null))
        .isEqualTo(expected);
  }

  @DisplayName(
      "raises a ServiceException when finding an UrlAlias leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenFindLeadsToAnException() throws UrlAliasRepositoryException {
    when(repo.find(any(SearchPageRequest.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.find(new SearchPageRequest());
        });
  }

  @DisplayName("can return a SearchPageResult")
  @Test
  public void returnSearchPageResult() throws UrlAliasRepositoryException, CudamiServiceException {
    SearchPageResponse<LocalizedUrlAliases> expected = new SearchPageResponse();
    LocalizedUrlAliases localizedUrlAlias = new LocalizedUrlAliases();
    localizedUrlAlias.add(
        createUrlAlias("hützligrütz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    expected.setContent(List.of(localizedUrlAlias));

    when(repo.find(any(SearchPageRequest.class))).thenReturn(expected);

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    assertThat(service.find(searchPageRequest)).isEqualTo(expected);
  }

  @DisplayName("can generate a slug without suffix")
  @Test
  public void generateSlugWithoutSuffix()
      throws CudamiServiceException, UrlAliasRepositoryException {
    String expected = "label";
    UUID websiteUuid = UUID.randomUUID();

    when(slugGenerator.generateSlug(eq("label"))).thenReturn("label");
    when(repo.hasUrlAlias(eq("label"), eq(websiteUuid), any(Locale.class))).thenReturn(false);

    assertThat(service.generateSlug(Locale.GERMAN, "label", websiteUuid)).isEqualTo(expected);
  }

  @DisplayName("throws an exception, when the query for existance of a slug leads to an exception")
  @Test
  public void throwsExceptionWhenSlugQueryFails() throws UrlAliasRepositoryException {
    when(repo.hasUrlAlias(any(String.class), any(UUID.class), any(Locale.class)))
        .thenThrow(new UrlAliasRepositoryException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.generateSlug(Locale.GERMAN, "label", UUID.randomUUID());
        });
  }

  @DisplayName("generates slugs with numeric suffix for default locale")
  @Test
  public void generateSlugWithNumericSuffixForDefaultLocale()
      throws UrlAliasRepositoryException, CudamiServiceException {
    String expected = "label-2";
    UUID websiteUuid = UUID.randomUUID();

    when(slugGenerator.generateSlug(eq("label"))).thenReturn("label");
    when(repo.hasUrlAlias(eq("label"), eq(websiteUuid), any(Locale.class))).thenReturn(true);
    when(repo.hasUrlAlias(eq("label-1"), eq(websiteUuid), any(Locale.class))).thenReturn(true);
    when(repo.hasUrlAlias(eq("label-2"), eq(websiteUuid), any(Locale.class))).thenReturn(false);

    assertThat(service.generateSlug(Locale.GERMAN, "label", websiteUuid)).isEqualTo(expected);
  }

  @DisplayName(
      "throws an exception, when the query for existance of a suffixed slug leads to an exception")
  @Test
  public void throwsExceptionWhenSlugQueryForSuffixesFails() throws UrlAliasRepositoryException {
    when(slugGenerator.generateSlug(eq("label"))).thenReturn("label");
    when(repo.hasUrlAlias(eq("label"), any(UUID.class), any(Locale.class))).thenReturn(true);

    when(repo.hasUrlAlias(eq("label-1"), any(UUID.class), any(Locale.class)))
        .thenThrow(new UrlAliasRepositoryException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.generateSlug(Locale.GERMAN, "label", UUID.randomUUID());
        });
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

  @DisplayName("does not filter anything when the LocalizedUrlAliases is empty")
  @Test
  public void noFilteringForEmptyLocalizedUrlAliases() {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    LocalizedUrlAliases actual =
        service.filterForLocaleWithFallback(Locale.forLanguageTag("de"), new LocalizedUrlAliases());
    assertThat(actual).isEqualTo(expected);
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

  @DisplayName("returns the generic primary link, when no website uuid is provided")
  @Test
  public void genericPrimaryLinkForNoWebsite()
      throws CudamiServiceException, UrlAliasRepositoryException {
    LocalizedUrlAliases expectedLocalizedUrlAliases = new LocalizedUrlAliases();
    expectedLocalizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    when(repo.findPrimaryLinksForWebsite(eq(null), eq("hurz")))
        .thenReturn(expectedLocalizedUrlAliases);

    LocalizedUrlAliases actual = service.findPrimaryLinks(null, "hurz", null);

    assertThat(actual).isEqualTo(expectedLocalizedUrlAliases);
  }

  @DisplayName("deleteForTarget with unset uuid deletes nothing and returns false")
  @Test
  public void deleteForTargetWithUuidNull()
      throws CudamiServiceException, UrlAliasRepositoryException {
    assertThat(service.deleteAllForTarget(null, true)).isFalse();
    verify(repo, never()).delete(any());
  }

  @DisplayName("deleteForTarget with force deletes everything")
  @Test
  public void deleteForTargetWithForce()
      throws CudamiServiceException, UrlAliasRepositoryException {
    UUID targetUuid = UUID.randomUUID();
    LocalizedUrlAliases targetLocalizedUrlAliases = new LocalizedUrlAliases();
    targetLocalizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));

    when(repo.findAllForTarget(eq(targetUuid))).thenReturn(targetLocalizedUrlAliases);
    when(repo.delete(any(List.class))).thenReturn(1);

    assertThat(service.deleteAllForTarget(targetUuid, true)).isTrue();

    ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(repo, times(1)).delete(listArgumentCaptor.capture());
    assertThat(listArgumentCaptor.getValue()).hasSize(1);
  }

  @DisplayName("checkPublication sets date to now for primary, if unset")
  @Test
  public void checkPublicationSetsNowForPrimaryIfUnset() throws CudamiServiceException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(true);
    urlAlias.setLastPublished(null);
    service.checkPublication(urlAlias);
    assertThat(urlAlias.getLastPublished()).isNotNull();
  }

  @DisplayName("checkPublication does not override an existing publication date")
  @Test
  public void checkPublicationDoesNotOverridePublicationDate()
      throws CudamiServiceException, UrlAliasRepositoryException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(true);
    LocalDateTime publicationDate = LocalDateTime.now();
    urlAlias.setLastPublished(publicationDate);

    when(repo.findOne(eq(urlAlias.getUuid()))).thenReturn(urlAlias);
    service.checkPublication(urlAlias);
    assertThat(urlAlias.getLastPublished()).isEqualTo(publicationDate);
  }

  @DisplayName("checkPublication throws an exception when changing an already published urlalias")
  @Test
  public void checkPublicationThrowsExceptionForAlreadyPublished()
      throws UrlAliasRepositoryException, CudamiServiceException {
    UrlAlias urlAlias =
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID());
    urlAlias.setPrimary(true);
    LocalDateTime publicationDate = LocalDateTime.now();
    urlAlias.setLastPublished(publicationDate);
    when(repo.findOne(eq(urlAlias.getUuid()))).thenReturn(urlAlias);

    UrlAlias changedUrlAlias = deepCopy(urlAlias);
    changedUrlAlias.setSlug("foo");

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.checkPublication(changedUrlAlias);
        });
  }

  @DisplayName("can successfully validate an empty LocalizedUrlAlias")
  @Test
  public void allowEmptyLocalizedUrlAlias() throws ValidationException {
    service.validate(null);
    service.validate(new LocalizedUrlAliases());
  }

  @DisplayName("can successfully validate a LocalizedUrlAlias with one primary UrlAlias")
  @Test
  public void allowOnePrimaryUrlAlias() throws ValidationException {
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    localizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", true, UUID.randomUUID(), UUID.randomUUID()));
    service.validate(localizedUrlAliases);
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
      "throws an exception when validating a LocalizedUrlAlias with two primary UrlAliases for the same website,target and language tuple")
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
    urlAlias.setTargetEntityType(EntityType.COLLECTION);
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

  private UrlAlias deepCopy(UrlAlias urlAlias) {
    UrlAlias copy = new UrlAlias();
    copy.setPrimary(urlAlias.isPrimary());
    copy.setTargetLanguage(urlAlias.getTargetLanguage());
    copy.setUuid(urlAlias.getUuid());
    copy.setCreated(urlAlias.getCreated());
    copy.setWebsite(urlAlias.getWebsite());
    copy.setLastPublished(urlAlias.getLastPublished());
    copy.setSlug(urlAlias.getSlug());
    copy.setTargetIdentifiableType(urlAlias.getTargetIdentifiableType());
    copy.setTargetEntityType(urlAlias.getTargetEntityType());
    return copy;
  }
}
