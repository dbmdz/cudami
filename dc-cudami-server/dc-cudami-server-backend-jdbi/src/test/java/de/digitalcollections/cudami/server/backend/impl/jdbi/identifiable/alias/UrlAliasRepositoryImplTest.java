package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias;

import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLanguage;
import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLocalesByScript;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractRepositoryImplTest;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = UrlAliasRepositoryImpl.class)
@DisplayName("The UrlAlias Repository")
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrlAliasRepositoryImplTest extends AbstractRepositoryImplTest {

  UrlAliasRepositoryImpl repo;
  @Autowired WebpageRepository webpageRepository;
  @Autowired WebsiteRepository websiteRepository;

  Website website;
  UrlAlias urlAliasWithoutWebsite;
  UrlAlias urlAliasWithWebsite;

  @BeforeAll
  public void setupTest() throws MalformedURLException, RepositoryException {
    this.repo = new UrlAliasRepositoryImpl(this.jdbi, cudamiConfig);
    this.prepareWebsite();
    this.urlAliasWithoutWebsite = this.getNewUrlAliasObject();
    this.urlAliasWithWebsite = this.getNewUrlAliasObject();
    this.urlAliasWithWebsite.setWebsite(this.website);
    this.urlAliasWithWebsite.setSlug("impressum-with-website");
  }

  private void prepareWebsite() throws MalformedURLException, RepositoryException {
    // to meet the foreign key constraints we must do some preparation
    this.website = new Website(new URL("https://my-first-website.com"));
    this.website.setUuid(UUID.randomUUID());
    this.website.setCreated(LocalDateTime.now());
    this.website.setRefId(13);
    this.website.setLastModified(LocalDateTime.now());
    this.website.setLabel("Test website");
    this.websiteRepository.save(this.website);
  }

  private UrlAlias getNewUrlAliasObject() throws RepositoryException {
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setSlug("impressum");
    urlAlias.setTargetLanguage(Locale.GERMAN);
    urlAlias.setTargetIdentifiableObjectType(IdentifiableObjectType.WEBPAGE);
    urlAlias.setTargetIdentifiableType(IdentifiableType.RESOURCE);
    Webpage webpage = Webpage.builder().label(Locale.GERMAN, "webpage").build();
    webpageRepository.save(webpage);
    urlAlias.setTargetUuid(webpage.getUuid());
    return urlAlias;
  }

  @DisplayName("Save an UrlAlias object")
  @Order(1)
  @Test
  public void save() throws RepositoryException {
    assertThat(this.urlAliasWithWebsite.getUuid()).isNull();
    assertThat(this.urlAliasWithWebsite.getCreated()).isNull();
    assertThat(this.urlAliasWithWebsite.getLastPublished()).isNull();

    repo.save(urlAliasWithWebsite);

    assertThat(urlAliasWithWebsite.getUuid()).isNotNull();
    assertThat(urlAliasWithWebsite.getCreated()).isNotNull();
    assertThat(urlAliasWithWebsite.getLastPublished()).isNull();
    assertFalse(urlAliasWithWebsite.isPrimary());
    assertEquals(urlAliasWithWebsite.getWebsite().getUuid(), this.website.getUuid());
    assertEquals(urlAliasWithWebsite.getWebsite().getLabel(), this.website.getLabel());
    assertEquals(urlAliasWithWebsite.getWebsite().getUrl(), this.website.getUrl());
  }

  @DisplayName("Retrieve object by UUID")
  @Order(2)
  @Test
  public void getByUuid() throws RepositoryException {
    UrlAlias found = this.repo.getByUuid(this.urlAliasWithWebsite.getUuid());
    assertEquals(found, this.urlAliasWithWebsite);
  }

  @DisplayName("Update an UrlAlias object")
  @Order(3)
  @Test
  public void update() throws RepositoryException {
    repo.save(urlAliasWithoutWebsite);
    urlAliasWithoutWebsite.setLastPublished(LocalDateTime.now());
    urlAliasWithoutWebsite.setPrimary(true);
    urlAliasWithoutWebsite.setTargetIdentifiableObjectType(IdentifiableObjectType.COLLECTION);
    urlAliasWithoutWebsite.setTargetIdentifiableType(IdentifiableType.ENTITY);

    UrlAlias beforeUpdate = createDeepCopy(urlAliasWithoutWebsite);

    repo.update(this.urlAliasWithoutWebsite);

    assertThat(urlAliasWithoutWebsite).isEqualTo(beforeUpdate);
  }

  @DisplayName("Retrieve LocalizedUrlAliases for target UUID")
  @Order(4)
  @Test
  public void findAllForTarget() throws RepositoryException {
    this.urlAliasWithWebsite = this.getNewUrlAliasObject();
    this.urlAliasWithWebsite.setSlug("wir_ueber_uns");
    this.urlAliasWithWebsite.setTargetUuid(this.urlAliasWithoutWebsite.getTargetUuid());
    this.urlAliasWithWebsite.setTargetIdentifiableType(IdentifiableType.RESOURCE);
    this.urlAliasWithWebsite.setWebsite(this.website);
    repo.save(this.urlAliasWithWebsite);

    LocalizedUrlAliases actual =
        this.repo.getAllForTarget(this.urlAliasWithoutWebsite.getTargetUuid());
    LocalizedUrlAliases expected =
        new LocalizedUrlAliases(this.urlAliasWithoutWebsite, this.urlAliasWithWebsite);
    assertTrue(actual.equals(expected));
  }

  @DisplayName("Retrieve main link for target UUID")
  @Order(5)
  @Test
  public void findMainLinks() throws RepositoryException {
    UrlAlias anotherMainLink = this.getNewUrlAliasObject();
    anotherMainLink.setTargetUuid(this.urlAliasWithoutWebsite.getTargetUuid());
    anotherMainLink.setTargetLanguage(Locale.ENGLISH);
    anotherMainLink.setSlug("another_main_link");
    anotherMainLink.setPrimary(true);
    anotherMainLink.setWebsite(this.website);
    this.repo.save(anotherMainLink);

    UrlAlias mainLinkInGerman = getNewUrlAliasObject();
    mainLinkInGerman.setTargetUuid(urlAliasWithoutWebsite.getTargetUuid());
    mainLinkInGerman.setSlug("main_link_in_german");
    mainLinkInGerman.setTargetLanguage(Locale.GERMAN);
    mainLinkInGerman.setPrimary(true);
    mainLinkInGerman.setWebsite(website);
    repo.save(mainLinkInGerman);

    LocalizedUrlAliases
        allLinks = this.repo.getAllForTarget(this.urlAliasWithoutWebsite.getTargetUuid()),
        mainLinksWithWebsite =
            this.repo.findPrimaryLinksForWebsite(this.website.getUuid(), "wir_ueber_uns", false),
        mainLinksConsideringLang =
            repo.findPrimaryLinksForWebsite(website.getUuid(), "wir_ueber_uns"),
        mainLinksWithoutWebsite = this.repo.findAllPrimaryLinks("wir_ueber_uns");

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

  @DisplayName("Generic search method")
  @Order(6)
  @Test
  public void find() throws RepositoryException {
    PageRequest pageRequest = new PageRequest("ueber", 0, 10);
    pageRequest.add(
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("targetLanguage")
                    .isEquals(Locale.GERMAN.toString())
                    .build())
            .build());

    PageResponse<LocalizedUrlAliases> pageResponse = this.repo.find(pageRequest);
    assertTrue(pageResponse.hasContent());
    assertThat(pageResponse.getTotalElements()).isEqualTo(1);
    assertThat(pageResponse.getContent().size()).isEqualTo(1);
    assertTrue(pageResponse.getContent().get(0).containsKey(Locale.GERMAN));
    assertThat(pageResponse.getContent().get(0).get(Locale.GERMAN))
        .isEqualTo(List.of(this.urlAliasWithWebsite));
  }

  @DisplayName("Check method hasUrlAlias")
  @Order(7)
  @Test
  public void hasUrlAlias() throws RepositoryException {
    assertThrows(
        RepositoryException.class,
        () -> this.repo.hasUrlAlias("", this.website.getUuid(), Locale.ROOT));
    assertFalse(
        this.repo.hasUrlAlias(
            this.urlAliasWithoutWebsite.getSlug(), this.website.getUuid(), Locale.ROOT));
    assertTrue(this.repo.hasUrlAlias(this.urlAliasWithoutWebsite.getSlug(), null, Locale.GERMAN));
    assertTrue(
        this.repo.hasUrlAlias(
            this.urlAliasWithWebsite.getSlug(), this.website.getUuid(), Locale.GERMAN));
    assertFalse(this.repo.hasUrlAlias("does_not_exist", this.website.getUuid(), Locale.ROOT));
  }

  @DisplayName("Delete an UrlAlias")
  @Order(8)
  @Test
  public void delete() throws RepositoryException {
    int count = this.repo.delete(List.of(this.urlAliasWithoutWebsite.getUuid()));
    assertThat(count).isEqualTo(1);
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
}
