package de.digitalcollections.cudami.server.backend.impl.jdbi.alias;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
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

  UUID websiteUuid = UUID.randomUUID();
  UrlAlias urlAlias = this.getNewUrlAliasObject();

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
    urlAlias.setTargetType(EntityType.ENTITY);
    urlAlias.setTargetUuid(UUID.randomUUID());
    return urlAlias;
  }

  @DisplayName("Save an UrlAlias object")
  @Order(1)
  @Test
  public void save() {
    assertThat(this.urlAlias.getUuid()).isNull();
    assertThat(this.urlAlias.getCreated()).isNull();
    assertThat(this.urlAlias.getLastPublished()).isNull();

    UrlAlias actual = this.repo.save(this.urlAlias);

    assertThat(actual.getUuid()).isNotNull();
    assertThat(actual.getCreated()).isNotNull();
    assertThat(actual.getLastPublished()).isNull();
    assertThat(actual.isMainAlias()).isEqualTo(false);
    this.urlAlias = actual;
  }

  @DisplayName("Retrieve object by UUID")
  @Order(2)
  @Test
  public void findOne() {
    UrlAlias found = this.repo.findOne(this.urlAlias.getUuid());
    assertThat(found).isEqualTo(this.urlAlias);
  }

  @DisplayName("Update an UrlAlias object")
  @Order(3)
  @Test
  public void update() {
    this.urlAlias.setLastPublished(LocalDateTime.now());
    this.urlAlias.setMainAlias(true);
    this.urlAlias.setTargetType(EntityType.COLLECTION);
    UrlAlias updated = this.repo.update(this.urlAlias);

    assertThat(updated).isEqualTo(this.urlAlias);
  }

  @DisplayName("Retrieve LocalizedUrlAliases for target UUID")
  @Order(4)
  @Test
  public void findAllForTarget() {
    UrlAlias secondUrlAlias = this.getNewUrlAliasObject();
    secondUrlAlias.setSlug("wir_ueber_uns");
    secondUrlAlias.setTargetUuid(this.urlAlias.getTargetUuid());
    secondUrlAlias = this.repo.save(secondUrlAlias);

    LocalizedUrlAliases actual = this.repo.findAllForTarget(this.urlAlias.getTargetUuid());
    LocalizedUrlAliases expected = new LocalizedUrlAliases(this.urlAlias, secondUrlAlias);
    assertThat(actual.keySet()).isEqualTo(expected.keySet());
    assertThat(actual.get(Locale.GERMAN)).containsAll(expected.get(Locale.GERMAN));
  }

  @DisplayName("Retrieve main link for target UUID")
  @Order(5)
  @Test
  public void findMainLink() {
    UrlAlias actual = this.repo.findMainLink(this.urlAlias.getTargetUuid());
    assertThat(actual).isEqualTo(this.urlAlias);
  }

  @DisplayName("Delete an UrlAlias")
  @Order(6)
  @Test
  public void delete() {
    int count = this.repo.delete(List.of(this.urlAlias.getUuid()));
    assert count == 1;
  }
}
