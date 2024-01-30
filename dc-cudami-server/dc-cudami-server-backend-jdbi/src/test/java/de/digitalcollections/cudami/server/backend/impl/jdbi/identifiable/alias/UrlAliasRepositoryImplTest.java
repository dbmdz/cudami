package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias;

import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLanguage;
import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLocalesByScript;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractRepositoryImplTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = UrlAliasRepositoryImpl.class)
@DisplayName("The UrlAlias Repository")
public class UrlAliasRepositoryImplTest extends AbstractRepositoryImplTest {

  private UrlAliasRepositoryImpl repo;
  @Autowired IdentifiableRepository<Identifiable> identifiableRepository;
  @Autowired WebpageRepository webpageRepository;
  @Autowired WebsiteRepository websiteRepository;

  @BeforeEach
  public void beforeEach() {
    repo = new UrlAliasRepositoryImpl(jdbi, cudamiConfig);
  }

  @DisplayName("can save and retrieve an UrlAlias for a webpage with website")
  @Test
  public void saveUrlAliasWithWebsite()
      throws MalformedURLException, RepositoryException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);
    Website website = ensurePresistedWebsite(webpage);

    UrlAlias urlAlias =
        UrlAlias.builder()
            .website(website)
            .slug("impressum-with-website")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .build();

    repo.save(urlAlias);

    Assertions.assertThat(urlAlias.getUuid()).isNotNull();
    Assertions.assertThat(urlAlias.getCreated()).isNotNull();
    Assertions.assertThat(urlAlias.getLastPublished()).isNull();

    UrlAlias actual = repo.getByUuid(urlAlias.getUuid());

    // We know, that getByUuid does NOT fill the Website object. So we fill it
    // manually in the "actual" object
    if (actual.getWebsite().getUuid().equals(website.getUuid())) {
      actual.setWebsite(website);
    }
    assertThat(actual.equals(urlAlias));
  }

  private Identifiable copyWebpageToIdentifiable(Webpage webpage) {
    Identifiable target = new Identifiable();
    target.setIdentifiableObjectType(webpage.getIdentifiableObjectType());
    target.setType(webpage.getType());
    target.setUuid(webpage.getUuid());
    return target;
  }

  @DisplayName("can save and retrieve an UrlAlias for a webpage without website")
  @Test
  public void saveUrlAliasWithoutWebsite() throws RepositoryException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);
    UrlAlias urlAlias =
        UrlAlias.builder().slug("impressum").targetLanguage(Locale.GERMAN).target(target).build();

    repo.save(urlAlias);

    Assertions.assertThat(urlAlias.getUuid()).isNotNull();
    Assertions.assertThat(urlAlias.getCreated()).isNotNull();
    Assertions.assertThat(urlAlias.getLastPublished()).isNull();

    UrlAlias actual = repo.getByUuid(urlAlias.getUuid());

    assertThat(actual.equals(urlAlias));
  }

  @DisplayName("can update an UrlAlias for a webpage without website")
  @Test
  public void updateWithoutWebsite() throws RepositoryException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);
    UrlAlias urlAlias =
        UrlAlias.builder().slug("impressum").targetLanguage(Locale.GERMAN).target(target).build();
    repo.save(urlAlias);

    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setPrimary(true);

    urlAlias.getTarget().setIdentifiableObjectType(IdentifiableObjectType.COLLECTION);
    urlAlias.getTarget().setType(IdentifiableType.ENTITY);

    UrlAlias beforeUpdate = createDeepCopy(urlAlias);

    repo.update(urlAlias);

    assertThat(urlAlias.equals(beforeUpdate));

    UrlAlias persisted = repo.getByUuid(urlAlias.getUuid());
    assertThat(persisted.equals(urlAlias));
  }

  @DisplayName("can update an UrlAlias for a webpage with website")
  @Test
  public void updateWithWebsite()
      throws RepositoryException, MalformedURLException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);
    Website website = ensurePresistedWebsite(webpage);

    UrlAlias urlAlias =
        UrlAlias.builder()
            .website(website)
            .slug("impressum-for-website")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .build();
    repo.save(urlAlias);

    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setPrimary(true);

    urlAlias.getTarget().setIdentifiableObjectType(IdentifiableObjectType.COLLECTION);
    urlAlias.getTarget().setType(IdentifiableType.ENTITY);

    UrlAlias beforeUpdate = createDeepCopy(urlAlias);

    repo.update(urlAlias);

    assertThat(urlAlias.equals(beforeUpdate));

    UrlAlias persisted = repo.getByUuid(urlAlias.getUuid());
    // We know, that getByUuid does NOT fill the Website object. So we fill it
    // manually in the "actual" object
    if (persisted.getWebsite().getUuid().equals(website.getUuid())) {
      persisted.setWebsite(website);
    }
    assertThat(persisted.equals(urlAlias));
  }

  @DisplayName("can retrieve for targetUuid")
  @Test
  public void retrieveForTargetUuid()
      throws RepositoryException, MalformedURLException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);

    UrlAlias urlAlias1 =
        UrlAlias.builder().slug("impressum").targetLanguage(Locale.GERMAN).target(webpage).build();
    repo.save(urlAlias1);

    Website website = ensurePresistedWebsite(webpage);
    UrlAlias urlAlias2 =
        UrlAlias.builder()
            .slug("impressum-for-website")
            .website(website)
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .build();
    repo.save(urlAlias2);

    LocalizedUrlAliases actual = repo.getByIdentifiable(webpage.getUuid());
    LocalizedUrlAliases expected = new LocalizedUrlAliases(urlAlias1, urlAlias2);
    assertThat(actual.equals(expected));
  }

  @DisplayName("can retrieve the main link for a target uuid")
  @Test
  public void findMainLinks()
      throws RepositoryException, MalformedURLException, ValidationException {
    Webpage webpageWirUeberUns = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpageWirUeberUns);
    Website website = ensurePresistedWebsite(webpageWirUeberUns);
    UrlAlias urlAliasWithoutWebsite =
        UrlAlias.builder()
            .slug("wir_ueber_uns")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .isPrimary()
            .build();
    repo.save(urlAliasWithoutWebsite);

    UrlAlias urlAliasWithWebsite =
        UrlAlias.builder()
            .website(website)
            .slug("wir_ueber_uns")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .build();
    repo.save(urlAliasWithWebsite);

    UrlAlias anotherMainLink =
        UrlAlias.builder()
            .website(website)
            .slug("another_main_link")
            .targetLanguage(Locale.ENGLISH)
            .target(target)
            .isPrimary()
            .build();
    repo.save(anotherMainLink);

    UrlAlias mainLinkInGerman =
        UrlAlias.builder()
            .website(website)
            .slug("main_link_in_german")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .isPrimary()
            .build();
    repo.save(mainLinkInGerman);

    LocalizedUrlAliases allLinks = repo.getByIdentifiable(target.getUuid()),
        mainLinksWithWebsite =
            repo.findPrimaryLinksForWebsite(website.getUuid(), "wir_ueber_uns", false),
        mainLinksConsideringLang =
            repo.findPrimaryLinksForWebsite(website.getUuid(), "wir_ueber_uns"),
        mainLinksWithoutWebsite = repo.findAllPrimaryLinks("wir_ueber_uns");

    assertThat(allLinks.flatten().size()).isEqualTo(4);
    assertThat(mainLinksWithWebsite.flatten().size()).isEqualTo(2);
    assertThat(mainLinksWithoutWebsite.flatten().size()).isEqualTo(3);
    assertThat(
            mainLinksWithoutWebsite
                .get(Locale.GERMAN)
                .containsAll(List.of(mainLinkInGerman, urlAliasWithoutWebsite)))
        .isTrue();
    assertThat(mainLinksWithoutWebsite.get(Locale.ENGLISH).get(0)).isEqualTo(anotherMainLink);
    assertThat(mainLinksConsideringLang.flatten().size()).isEqualTo(1);
  }

  @DisplayName("can find filtered UrlAliases")
  @Test
  public void find() throws RepositoryException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);
    UrlAlias urlAliasUeber =
        UrlAlias.builder()
            .slug("ueber")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .isPrimary()
            .build();
    repo.save(urlAliasUeber);

    UrlAlias urlAliasUnter =
        UrlAlias.builder().slug("unter").targetLanguage(Locale.GERMAN).target(webpage).build();
    repo.save(urlAliasUnter);

    PageRequest pageRequest = new PageRequest("ueber", 0, 10);
    pageRequest.add(
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("targetLanguage")
                    .isEquals(Locale.GERMAN.toString())
                    .build())
            .build());

    PageResponse<LocalizedUrlAliases> pageResponse = repo.findLocalizedUrlAliases(pageRequest);
    assertTrue(pageResponse.hasContent());
    assertThat(pageResponse.getTotalElements()).isEqualTo(1);
    assertThat(pageResponse.getContent().size()).isEqualTo(1);
    assertTrue(pageResponse.getContent().get(0).containsKey(Locale.GERMAN));
    assertThat(pageResponse.getContent().get(0).get(Locale.GERMAN))
        .isEqualTo(List.of(urlAliasUeber));
  }

  @DisplayName("Check method hasUrlAlias")
  @Test
  public void hasUrlAlias() throws RepositoryException, MalformedURLException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);
    Website website = ensurePresistedWebsite(webpage);
    UrlAlias urlAliasWithoutWebsite =
        UrlAlias.builder()
            .slug("wir_ueber_uns")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .build();
    repo.save(urlAliasWithoutWebsite);
    UrlAlias urlAliasWithWebsite =
        UrlAlias.builder()
            .website(website)
            .slug("wir_ueber_uns")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .build();
    repo.save(urlAliasWithWebsite);

    assertThrows(
        RepositoryException.class, () -> repo.hasUrlAlias("", website.getUuid(), Locale.ROOT));
    assertThat(repo.hasUrlAlias(urlAliasWithWebsite.getSlug(), website.getUuid(), Locale.ROOT))
        .isFalse();
    assertThat(repo.hasUrlAlias(urlAliasWithoutWebsite.getSlug(), (UUID) null, Locale.GERMAN))
        .isTrue();
    assertThat(repo.hasUrlAlias(urlAliasWithWebsite.getSlug(), website.getUuid(), Locale.GERMAN))
        .isTrue();
    assertThat(repo.hasUrlAlias("does_not_exist", website.getUuid(), Locale.ROOT)).isFalse();
  }

  @DisplayName("can delete an UrlAlias")
  @Test
  public void delete() throws RepositoryException, ValidationException {
    Webpage webpage = ensurePersistedWebpage();
    Identifiable target = copyWebpageToIdentifiable(webpage);
    UrlAlias urlAliasWithoutWebsite =
        UrlAlias.builder()
            .slug("wir_ueber_uns")
            .targetLanguage(Locale.GERMAN)
            .target(target)
            .build();
    repo.save(urlAliasWithoutWebsite);
    int count = repo.deleteByUuids(List.of(urlAliasWithoutWebsite.getUuid()));
    assertThat(count).isEqualTo(1);
  }

  @DisplayName("deleteByIdentifiable with force deletes everything")
  @Test
  public void deleteByIdentifiableWithForce() throws RepositoryException, ValidationException {
    Identifiable targetIdentifiable = createIdentifiable();
    LocalizedUrlAliases targetLocalizedUrlAliases = new LocalizedUrlAliases();
    targetLocalizedUrlAliases.add(
        createUrlAlias("hurz", true, "de", false, UUID.randomUUID(), UUID.randomUUID()));
    identifiableRepository.save(targetIdentifiable);

    repo.deleteByIdentifiable(targetIdentifiable, true);
    LocalizedUrlAliases localizedUrlAliases = repo.getByIdentifiable(targetIdentifiable);

    assertThat(localizedUrlAliases).hasSize(0);
  }

  private static Stream<Arguments> testGrabLanguage() {
    return Stream.of(
        Arguments.of(Locale.ROOT, "und"),
        Arguments.of(new Locale("und"), "und"),
        Arguments.of(Locale.GERMANY, "de"),
        Arguments.of(Locale.ENGLISH, "en"),
        Arguments.of(new Locale.Builder().setScript("Hani").build(), "und"),
        Arguments.of(new Locale.Builder().setLanguage("en").setScript("Latn").build(), "en"));
  }

  @DisplayName("Test grabLanguage")
  @ParameterizedTest
  @MethodSource
  public void testGrabLanguage(Locale locale, String expected) {
    assertThat(grabLanguage(locale)).isEqualTo(expected);
  }

  @DisplayName("Test that any scripts are ignored that are not '' or 'Latn'")
  @Test
  public void testGrabLocalesByScript() {
    var locales =
        List.of(
            new Locale.Builder().setLanguage("und").setScript("Latn").build(),
            new Locale.Builder().setLanguage("zh").setScript("Hani").build(),
            new Locale.Builder().setLanguage("en").build());
    assertThat(grabLocalesByScript(locales))
        .containsExactlyInAnyOrder(locales.get(0), locales.get(2));
  }

  // --------------------------------------------------------------------
  private Webpage ensurePersistedWebpage() throws RepositoryException, ValidationException {
    Webpage webpage = Webpage.builder().label(Locale.GERMAN, "webpage").build();
    webpageRepository.save(webpage);
    return webpage;
  }

  private Website ensurePresistedWebsite(Webpage... rootPages)
      throws MalformedURLException, RepositoryException, ValidationException {
    Website website =
        Website.builder()
            .url("https://my-first-website.com")
            .refId(13)
            .label("Test website")
            .rootPages(Arrays.asList(rootPages))
            .build();
    websiteRepository.save(website);
    return website;
  }
}
