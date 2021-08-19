package de.digitalcollections.cudami.server.backend.impl.jdbi.alias;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.filter.FilteringBuilder;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.paging.SearchPageRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;
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

  UUID websiteUuid = UUID.randomUUID();
  UrlAlias firstUrlAlias = this.getNewUrlAliasObject();

  UrlAlias secondUrlAlias;

  @BeforeAll
  public void setupTest() {
    this.repo = new UrlAliasRepositoryImpl(this.jdbi);
    this.prepareWebsite();
  }

  private void prepareWebsite() {
    // to meet the foreign key constraints we must do some preparation
    try {
      Website website = new Website(new URL("https://my-first-website.com"));
      website.setUuid(this.websiteUuid);
      website.setCreated(LocalDateTime.now());
      website.setRefId(13);
      website.setLastModified(LocalDateTime.now());
      website.setLabel("Test website");
      this.websiteRepository.save(website);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  private UrlAlias getNewUrlAliasObject() {
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setWebsiteUuid(this.websiteUuid);
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
    assertThat(this.firstUrlAlias.getUuid()).isNull();
    assertThat(this.firstUrlAlias.getCreated()).isNull();
    assertThat(this.firstUrlAlias.getLastPublished()).isNull();

    UrlAlias actual = this.repo.save(this.firstUrlAlias);

    assertThat(actual.getUuid()).isNotNull();
    assertThat(actual.getCreated()).isNotNull();
    assertThat(actual.getLastPublished()).isNull();
    assertThat(actual.isMainAlias()).isEqualTo(false);
    this.firstUrlAlias = actual;
  }

  @DisplayName("Retrieve object by UUID")
  @Order(2)
  @Test
  public void findOne() throws UrlAliasRepositoryException {
    UrlAlias found = this.repo.findOne(this.firstUrlAlias.getUuid());
    assertThat(found).isEqualTo(this.firstUrlAlias);
  }

  @DisplayName("Update an UrlAlias object")
  @Order(3)
  @Test
  public void update() throws UrlAliasRepositoryException {
    this.firstUrlAlias.setLastPublished(LocalDateTime.now());
    this.firstUrlAlias.setMainAlias(true);
    this.firstUrlAlias.setTargetIdentifiableType(IdentifiableType.ENTITY);
    this.firstUrlAlias.setTargetEntityType(EntityType.COLLECTION);
    UrlAlias updated = this.repo.update(this.firstUrlAlias);

    assertThat(updated).isEqualTo(this.firstUrlAlias);
  }

  @DisplayName("Retrieve LocalizedUrlAliases for target UUID")
  @Order(4)
  @Test
  public void findAllForTarget() throws UrlAliasRepositoryException {
    this.secondUrlAlias = this.getNewUrlAliasObject();
    this.secondUrlAlias.setSlug("wir_ueber_uns");
    this.secondUrlAlias.setTargetUuid(this.firstUrlAlias.getTargetUuid());
    this.secondUrlAlias.setTargetIdentifiableType(IdentifiableType.RESOURCE);
    this.secondUrlAlias = this.repo.save(this.secondUrlAlias);

    LocalizedUrlAliases actual = this.repo.findAllForTarget(this.firstUrlAlias.getTargetUuid());
    LocalizedUrlAliases expected = new LocalizedUrlAliases(this.firstUrlAlias, this.secondUrlAlias);
    assertThat(actual.keySet()).isEqualTo(expected.keySet());
    assertThat(actual.get(Locale.GERMAN)).containsAll(expected.get(Locale.GERMAN));
  }

  @DisplayName("Retrieve main link for target UUID")
  @Order(5)
  @Test
  public void findMainLinks() throws UrlAliasRepositoryException {
    UrlAlias anotherMainLink = this.getNewUrlAliasObject();
    anotherMainLink.setTargetUuid(this.firstUrlAlias.getTargetUuid());
    anotherMainLink.setTargetLanguage(Locale.ENGLISH);
    anotherMainLink.setSlug("another_main_link");
    anotherMainLink.setMainAlias(true);
    anotherMainLink = this.repo.save(anotherMainLink);

    LocalizedUrlAliases allLinks = this.repo.findAllForTarget(this.firstUrlAlias.getTargetUuid()),
        mainLinks = this.repo.findMainLinks(this.websiteUuid, "wir_ueber_uns");

    assert allLinks.values().stream().flatMapToInt(list -> IntStream.of(list.size())).sum() == 3;
    assert mainLinks.values().stream().flatMapToInt(list -> IntStream.of(list.size())).sum() == 2;
    assertThat(mainLinks.get(Locale.GERMAN).get(0)).isEqualTo(this.firstUrlAlias);
    assertThat(mainLinks.get(Locale.ENGLISH).get(0)).isEqualTo(anotherMainLink);
  }

  @DisplayName("Generic search method")
  @Order(6)
  @Test
  public void find() throws UrlAliasRepositoryException {
    var searchPageRequest = new SearchPageRequest("ueber", 0, 10);
    searchPageRequest.add(
        new FilteringBuilder().filter("targetLanguage").isEquals(Locale.GERMAN.toString()).build());

    var searchPageResponse = this.repo.find(searchPageRequest);
    assert searchPageResponse.hasContent();
    assert searchPageResponse.getTotalElements() == 1;
    assert searchPageResponse.getContent().size() == 1;
    assert searchPageResponse.getContent().get(0).containsKey(Locale.GERMAN);
    assertThat(searchPageResponse.getContent().get(0).get(Locale.GERMAN))
        .isEqualTo(List.of(this.secondUrlAlias));
  }

  @DisplayName("Delete an UrlAlias")
  @Order(7)
  @Test
  public void delete() throws UrlAliasRepositoryException {
    int count = this.repo.delete(List.of(this.firstUrlAlias.getUuid()));
    assert count == 1;
  }
}
