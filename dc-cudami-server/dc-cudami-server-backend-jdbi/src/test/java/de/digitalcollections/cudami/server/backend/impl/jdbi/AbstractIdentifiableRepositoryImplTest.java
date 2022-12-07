package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
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
public abstract class AbstractIdentifiableRepositoryImplTest<R extends IdentifiableRepository> {

  @Autowired protected PostgreSQLContainer postgreSQLContainer;
  @Autowired protected Jdbi jdbi;
  @Autowired protected CudamiConfig cudamiConfig;
  @Autowired private DigitalCollectionsObjectMapper mapper;

  protected R repo;

  protected static final Locale LOCALE_ZH_HANI =
      new Locale.Builder().setLanguage("zh").setScript("Hani").build();

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

  protected <I extends Identifiable> void saveAndAssertTimestampsAndEqualityToSaveable(
      I identifiable) {
    repo.save(identifiable);
    Assertions.assertThat(identifiable.getUuid()).isNotNull();
    Assertions.assertThat(identifiable.getCreated()).isNotNull();
    Assertions.assertThat(identifiable.getLastModified()).isNotNull();

    I actual = (I) repo.getByUuid(identifiable.getUuid());

    assertThat(actual).isEqualToComparingFieldByField(identifiable);
  }

  protected <I extends Identifiable> void updateAndAssertUpdatedLastModifiedTimestamp(
      I identifiable) {
    LocalDateTime timestampBeforeUpdate = LocalDateTime.now();
    repo.update(identifiable);
    LocalDateTime timestampAfterUpdate = LocalDateTime.now();

    // The last modified timestamp must be modified and must be between the time before and
    // after the uodate
    Assertions.assertThat(identifiable.getLastModified()).isAfter(timestampBeforeUpdate);
    Assertions.assertThat(identifiable.getLastModified()).isBefore(timestampAfterUpdate);
  }

  protected <I extends Identifiable> void assertInDatabaseIsEqualToUpdateable(
      I identifiable, I identifiableBeforeUpdate, Function<I, I> additionalFilling) {
    I actual = (I) repo.getByUuid(identifiable.getUuid());
    actual = additionalFilling.apply(actual);

    // Now replay the timestamp change
    identifiableBeforeUpdate.setLastModified(actual.getLastModified());
    // Verify, that the data to be persisted is equal with the data persisted
    assertThat(actual).isEqualTo(identifiableBeforeUpdate);
  }
}
