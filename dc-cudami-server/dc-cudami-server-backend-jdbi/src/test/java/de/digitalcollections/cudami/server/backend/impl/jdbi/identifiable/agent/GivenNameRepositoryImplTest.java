package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.identifiable.agent.GivenName.Gender;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Locale;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = GivenNameRepositoryImpl.class)
@DisplayName("The GivenName Repository")
class GivenNameRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<GivenNameRepositoryImpl> {
  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;

  @BeforeEach
  public void beforeEach() {
    repo =
        new GivenNameRepositoryImpl(jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
  }

  @Test
  @DisplayName("can save and fill uuid and timestamps")
  void testSave() throws RepositoryException, ValidationException {
    GivenName givenName =
        GivenName.builder().label(Locale.GERMAN, "Karl Ranseier").gender(Gender.MALE).build();
    saveAndAssertTimestampsAndEqualityToSaveable(givenName);
  }

  @Test
  @DisplayName("can update and modify lastModified timestamp")
  void testUpdate() throws RepositoryException, ValidationException {
    GivenName givenName =
        GivenName.builder().label(Locale.GERMAN, "Karl Ranseier").gender(Gender.MALE).build();
    repo.save(givenName);

    // We change the gender now
    givenName.setGender(Gender.UNISEX);
    GivenName beforeUpdate = createDeepCopy(givenName);

    updateAndAssertUpdatedLastModifiedTimestamp(givenName);
    assertInDatabaseIsEqualToUpdateable(givenName, beforeUpdate, Function.identity());
  }
}
