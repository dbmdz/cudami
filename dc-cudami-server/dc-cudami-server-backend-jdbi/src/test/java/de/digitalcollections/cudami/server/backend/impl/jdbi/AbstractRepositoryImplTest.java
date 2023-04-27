package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
public class AbstractRepositoryImplTest {

  @Autowired protected PostgreSQLContainer postgreSQLContainer;
  @Autowired protected Jdbi jdbi;
  @Autowired protected CudamiConfig cudamiConfig;
  @Autowired private DigitalCollectionsObjectMapper mapper;

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  protected <O> O createDeepCopy(O object) {
    try {
      String serializedObject = mapper.writeValueAsString(object);
      O copy = (O) mapper.readValue(serializedObject, object.getClass());

      assertThat(copy).isEqualTo(object);
      return copy;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Cannot serialize/deserialize " + object + ": " + e, e);
    }
  }

  protected Identifiable createIdentifiable() {
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(UUID.randomUUID());
    identifiable.setType(IdentifiableType.ENTITY);
    identifiable.setLabel("label");
    return identifiable;
  }

  protected UrlAlias createUrlAlias(
      String slug,
      boolean setUuid,
      String language,
      boolean primary,
      UUID targetUuid,
      UUID websiteUuid) {
    Identifiable target = createIdentifiable();
    target.setUuid(targetUuid);
    target.setIdentifiableObjectType(IdentifiableObjectType.COLLECTION);
    target.setType(IdentifiableType.ENTITY);

    UrlAlias urlAlias = new UrlAlias();
    if (setUuid) {
      urlAlias.setUuid(UUID.randomUUID());
    }
    urlAlias.setPrimary(primary);
    urlAlias.setTarget(target);
    urlAlias.setSlug(slug);
    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setCreated(LocalDateTime.now());
    urlAlias.setTargetLanguage(Locale.forLanguageTag(language));
    urlAlias.setWebsite(createWebsite(websiteUuid));
    return urlAlias;
  }

  protected Website createWebsite(UUID uuid) {
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
}
