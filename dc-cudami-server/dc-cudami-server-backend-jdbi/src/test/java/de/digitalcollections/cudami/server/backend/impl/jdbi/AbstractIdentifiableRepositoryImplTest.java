package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Function;
import org.assertj.core.api.Assertions;

public abstract class AbstractIdentifiableRepositoryImplTest<R extends IdentifiableRepository>
    extends AbstractRepositoryImplTest {

  protected R repo;

  protected static final Locale LOCALE_UND_LATN =
      new Locale.Builder().setLanguage("und").setScript("Latn").build();

  protected static final Locale LOCALE_ZH_HANI =
      new Locale.Builder().setLanguage("zh").setScript("Hani").build();

  protected <I extends Identifiable> void saveAndAssertTimestampsAndEqualityToSaveable(
      I identifiable) throws RepositoryException, ValidationException {
    repo.save(identifiable);
    Assertions.assertThat(identifiable.getUuid()).isNotNull();
    Assertions.assertThat(identifiable.getCreated()).isNotNull();
    Assertions.assertThat(identifiable.getLastModified()).isNotNull();

    I actual = (I) repo.getByUuid(identifiable.getUuid());

    assertThat(actual).isEqualToComparingFieldByField(identifiable);
  }

  protected <I extends Identifiable> void updateAndAssertUpdatedLastModifiedTimestamp(
      I identifiable) throws RepositoryException, ValidationException {
    LocalDateTime timestampBeforeUpdate = LocalDateTime.now();
    repo.update(identifiable);
    LocalDateTime timestampAfterUpdate = LocalDateTime.now();

    // The last modified timestamp must be modified and must be between the time before and
    // after the uodate
    Assertions.assertThat(identifiable.getLastModified()).isAfter(timestampBeforeUpdate);
    Assertions.assertThat(identifiable.getLastModified()).isBefore(timestampAfterUpdate);
  }

  protected <I extends Identifiable> void assertInDatabaseIsEqualToUpdateable(
      I identifiable, I identifiableBeforeUpdate, Function<I, I> additionalFilling)
      throws RepositoryException {
    I actual = (I) repo.getByUuid(identifiable.getUuid());
    actual = additionalFilling.apply(actual);

    // Now replay the timestamp change
    identifiableBeforeUpdate.setLastModified(actual.getLastModified());
    // Verify, that the data to be persisted is equal with the data persisted
    assertThat(actual).isEqualTo(identifiableBeforeUpdate);
  }
}
