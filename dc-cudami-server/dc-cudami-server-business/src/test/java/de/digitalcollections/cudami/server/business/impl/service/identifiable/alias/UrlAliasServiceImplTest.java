package de.digitalcollections.cudami.server.business.impl.service.identifiable.alias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.commons.web.SlugGenerator;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
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

@DisplayName("The UrlAliasService implementation")
class UrlAliasServiceImplTest {

  private UrlAliasServiceImpl service;

  private UrlAliasRepository repo;

  private SlugGenerator slugGenerator;

  @BeforeEach
  public void beforeEach() {
    repo = mock(UrlAliasRepository.class);
    slugGenerator = mock(SlugGenerator.class);
    when(slugGenerator.generateSlug(any(String.class))).thenReturn("slug");
    service = new UrlAliasServiceImpl(repo, slugGenerator);
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
    UrlAlias expected = createUrlAlias("hützligrütz", false);

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
  public void raiseExceptionWhenSaveWithUuid() throws CudamiServiceException {
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.create(createUrlAlias("hützligrütz", true));
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
          service.create(createUrlAlias("hützligrütz", false));
        });
  }

  @DisplayName("creates and saves an UrlAlias and returns it with set UUID")
  @Test
  public void saveUrlAlias() throws CudamiServiceException, UrlAliasRepositoryException {
    UrlAlias urlAlias = createUrlAlias("hützligrütz", false);
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
          service.update(createUrlAlias("hützligrütz", false));
        });
  }

  @DisplayName("raises a ServiceException when updating leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenUpdateLeadsToAnException()
      throws CudamiServiceException, UrlAliasRepositoryException {
    when(repo.update(any(UrlAlias.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.update(createUrlAlias("hützligrütz", true));
        });
  }

  @DisplayName("updates and returns an UrlAlias")
  @Test
  public void updateUrlAlias() throws CudamiServiceException, UrlAliasRepositoryException {
    UrlAlias expected = createUrlAlias("hützligrütz", true);

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
    expected.add(createUrlAlias("hützligrütz", true));
    when(repo.findPrimaryLinksForWebsite(eq(uuid), eq(slug))).thenReturn(null);
    when(repo.findPrimaryLinksForWebsite(eq(null), eq(slug))).thenReturn(expected);

    assertThat(service.findPrimaryLinks(uuid, slug)).isEqualTo(expected);
  }

  @DisplayName(
      "raises a ServiceException when trying to get the primary links with a missing or empty slug")
  @Test
  public void raiseExceptionForPrimaryLinksWithMissingOrEmptySlug() throws CudamiServiceException {
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findPrimaryLinks(UUID.randomUUID(), null);
        });
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findPrimaryLinks(UUID.randomUUID(), "");
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
          service.findPrimaryLinks(UUID.randomUUID(), "hützligrütz");
        });
  }

  @DisplayName("can return primary links")
  @Test
  public void returnPrimaryLinks() throws CudamiServiceException, UrlAliasRepositoryException {
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    expected.add(createUrlAlias("hützligrütz", true));
    when(repo.findPrimaryLinksForWebsite(any(UUID.class), any(String.class))).thenReturn(expected);

    assertThat(service.findPrimaryLinks(UUID.randomUUID(), "hützligrütz")).isEqualTo(expected);
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
    localizedUrlAlias.add(createUrlAlias("hützligrütz", true));
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
    when(repo.hasUrlAlias(eq(websiteUuid), eq("label"))).thenReturn(false);

    assertThat(service.generateSlug(Locale.GERMAN, "label", websiteUuid)).isEqualTo(expected);
  }

  @DisplayName("throws an exception, when the query for existance of a slug leads to an exception")
  @Test
  public void throwsExceptionWhenSlugQueryFails() throws UrlAliasRepositoryException {
    when(repo.hasUrlAlias(any(UUID.class), any(String.class)))
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
    when(repo.hasUrlAlias(eq(websiteUuid), eq("label"))).thenReturn(true);
    when(repo.hasUrlAlias(eq(websiteUuid), eq("label-1"))).thenReturn(true);
    when(repo.hasUrlAlias(eq(websiteUuid), eq("label-2"))).thenReturn(false);

    assertThat(service.generateSlug(Locale.GERMAN, "label", websiteUuid)).isEqualTo(expected);
  }

  // -------------------------------------------------------------------------
  private UrlAlias createUrlAlias(String slug, boolean setUuid) {
    UrlAlias urlAlias = new UrlAlias();
    if (setUuid) {
      urlAlias.setUuid(UUID.randomUUID());
    }
    urlAlias.setPrimary(false);
    urlAlias.setTargetUuid(UUID.randomUUID());
    urlAlias.setSlug(slug);
    urlAlias.setTargetIdentifiableType(IdentifiableType.ENTITY);
    urlAlias.setTargetEntityType(EntityType.COLLECTION);
    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setCreated(LocalDateTime.now());
    urlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    urlAlias.setWebsite(createWebsite(UUID.randomUUID()));
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
