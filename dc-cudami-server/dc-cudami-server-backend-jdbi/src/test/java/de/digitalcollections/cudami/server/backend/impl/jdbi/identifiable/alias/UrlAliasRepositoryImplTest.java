package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.filter.FilteringBuilder;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.paging.SearchPageRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = UrlAliasRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The UrlAlias Repository")
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrlAliasRepositoryImplTest {

  UrlAliasRepositoryImpl repo;
  @Autowired Jdbi jdbi;
  @Autowired WebsiteRepository websiteRepository;

  Website website;
  UrlAlias urlAliasWithoutWebsite;
  UrlAlias urlAliasWithWebsite;

  @BeforeAll
  public void setupTest() throws MalformedURLException {
    this.repo = new UrlAliasRepositoryImpl(this.jdbi);
    this.prepareWebsite();
    this.urlAliasWithoutWebsite = this.getNewUrlAliasObject();
    this.urlAliasWithWebsite = this.getNewUrlAliasObject();
    this.urlAliasWithWebsite.setWebsite(this.website);
    this.urlAliasWithWebsite.setSlug("impressum-with-website");
  }

  private void prepareWebsite() throws MalformedURLException {
    // to meet the foreign key constraints we must do some preparation
    this.website = new Website(new URL("https://my-first-website.com"));
    this.website.setUuid(UUID.randomUUID());
    this.website.setCreated(LocalDateTime.now());
    this.website.setRefId(13);
    this.website.setLastModified(LocalDateTime.now());
    this.website.setLabel("Test website");
    this.websiteRepository.save(this.website);
  }

  private UrlAlias getNewUrlAliasObject() {
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setSlug("impressum");
    urlAlias.setTargetLanguage(Locale.GERMAN);
    urlAlias.setTargetIdentifiableType(IdentifiableType.RESOURCE);
    urlAlias.setTargetUuid(UUID.randomUUID());
    return urlAlias;
  }

  @DisplayName("Save an UrlAlias object")
  @Order(1)
  @Test
  public void save() throws UrlAliasRepositoryException {
    assertThat(this.urlAliasWithWebsite.getUuid()).isNull();
    assertThat(this.urlAliasWithWebsite.getCreated()).isNull();
    assertThat(this.urlAliasWithWebsite.getLastPublished()).isNull();

    UrlAlias actual = this.repo.save(this.urlAliasWithWebsite);

    assertThat(actual.getUuid()).isNotNull();
    assertThat(actual.getCreated()).isNotNull();
    assertThat(actual.getLastPublished()).isNull();
    assertFalse(actual.isPrimary());
    assertEquals(actual.getWebsite().getUuid(), this.website.getUuid());
    assertEquals(actual.getWebsite().getLabel(), this.website.getLabel());
    assertEquals(actual.getWebsite().getUrl(), this.website.getUrl());
    this.urlAliasWithWebsite = actual;
  }

  @DisplayName("Retrieve object by UUID")
  @Order(2)
  @Test
  public void findOne() throws UrlAliasRepositoryException {
    UrlAlias found = this.repo.findOne(this.urlAliasWithWebsite.getUuid());
    assertEquals(found, this.urlAliasWithWebsite);
  }

  @DisplayName("Update an UrlAlias object")
  @Order(3)
  @Test
  public void update() throws UrlAliasRepositoryException {
    this.urlAliasWithoutWebsite = this.repo.save(this.urlAliasWithoutWebsite);
    this.urlAliasWithoutWebsite.setLastPublished(LocalDateTime.now());
    this.urlAliasWithoutWebsite.setPrimary(true);
    this.urlAliasWithoutWebsite.setTargetIdentifiableType(IdentifiableType.ENTITY);
    this.urlAliasWithoutWebsite.setTargetEntityType(EntityType.COLLECTION);
    UrlAlias updated = this.repo.update(this.urlAliasWithoutWebsite);

    assertThat(updated).isEqualTo(this.urlAliasWithoutWebsite);
  }

  @DisplayName("Retrieve LocalizedUrlAliases for target UUID")
  @Order(4)
  @Test
  public void findAllForTarget() throws UrlAliasRepositoryException {
    this.urlAliasWithWebsite = this.getNewUrlAliasObject();
    this.urlAliasWithWebsite.setSlug("wir_ueber_uns");
    this.urlAliasWithWebsite.setTargetUuid(this.urlAliasWithoutWebsite.getTargetUuid());
    this.urlAliasWithWebsite.setTargetIdentifiableType(IdentifiableType.RESOURCE);
    this.urlAliasWithWebsite.setWebsite(this.website);
    this.urlAliasWithWebsite = this.repo.save(this.urlAliasWithWebsite);

    LocalizedUrlAliases actual =
        this.repo.findAllForTarget(this.urlAliasWithoutWebsite.getTargetUuid());
    LocalizedUrlAliases expected =
        new LocalizedUrlAliases(this.urlAliasWithoutWebsite, this.urlAliasWithWebsite);
    assertTrue(actual.equals(expected));
  }

  @DisplayName("Retrieve main link for target UUID")
  @Order(5)
  @Test
  public void findMainLinks() throws UrlAliasRepositoryException {
    UrlAlias anotherMainLink = this.getNewUrlAliasObject();
    anotherMainLink.setTargetUuid(this.urlAliasWithoutWebsite.getTargetUuid());
    anotherMainLink.setTargetLanguage(Locale.ENGLISH);
    anotherMainLink.setSlug("another_main_link");
    anotherMainLink.setPrimary(true);
    anotherMainLink.setWebsite(this.website);
    anotherMainLink = this.repo.save(anotherMainLink);

    LocalizedUrlAliases
        allLinks = this.repo.findAllForTarget(this.urlAliasWithoutWebsite.getTargetUuid()),
        mainLinksWithWebsite =
            this.repo.findPrimaryLinksForWebsite(this.website.getUuid(), "wir_ueber_uns", false),
        mainLinksWithoutWebsite = this.repo.findAllPrimaryLinks("wir_ueber_uns");

    assertThat(allLinks.flatten().size()).isEqualTo(3);
    assertThat(mainLinksWithWebsite.flatten().size()).isEqualTo(1);
    assertThat(mainLinksWithoutWebsite.flatten().size()).isEqualTo(2);
    assertThat(mainLinksWithoutWebsite.get(Locale.GERMAN).get(0))
        .isEqualTo(this.urlAliasWithoutWebsite);
    assertThat(mainLinksWithoutWebsite.get(Locale.ENGLISH).get(0)).isEqualTo(anotherMainLink);
  }

  @DisplayName("Generic search method")
  @Order(6)
  @Test
  public void find() throws UrlAliasRepositoryException {
    var searchPageRequest = new SearchPageRequest("ueber", 0, 10);
    searchPageRequest.add(
        new FilteringBuilder().filter("targetLanguage").isEquals(Locale.GERMAN.toString()).build());

    var searchPageResponse = this.repo.find(searchPageRequest);
    assertTrue(searchPageResponse.hasContent());
    assertThat(searchPageResponse.getTotalElements()).isEqualTo(1);
    assertThat(searchPageResponse.getContent().size()).isEqualTo(1);
    assertTrue(searchPageResponse.getContent().get(0).containsKey(Locale.GERMAN));
    assertThat(searchPageResponse.getContent().get(0).get(Locale.GERMAN))
        .isEqualTo(List.of(this.urlAliasWithWebsite));
  }

  @DisplayName("Check method hasUrlAlias")
  @Order(7)
  @Test
  public void hasUrlAlias() throws UrlAliasRepositoryException {
    assertThrows(
        UrlAliasRepositoryException.class,
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
  public void delete() throws UrlAliasRepositoryException {
    int count = this.repo.delete(List.of(this.urlAliasWithoutWebsite.getUuid()));
    assertThat(count).isEqualTo(1);
  }
}
