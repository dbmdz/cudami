package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Locale;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = FamilyNameRepositoryImpl.class)
@DisplayName("The FamiliyName Repository")
class FamilyNameRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<FamilyNameRepositoryImpl> {

  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;

  @BeforeEach
  public void beforeEach() {
    repo =
        new FamilyNameRepositoryImpl(jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
  }

  @Test
  @DisplayName("can save a FamilyName and fill uuid and timestamps")
  void testSave() throws RepositoryException, ValidationException {
    FamilyName familyName = FamilyName.builder().label(Locale.GERMAN, "Ranseier").build();
    saveAndAssertTimestampsAndEqualityToSaveable(familyName);
  }

  @Test
  @DisplayName("can update and modify lastModified timestamp")
  void testUpdate() throws RepositoryException, ValidationException {
    FamilyName familyName = FamilyName.builder().label(Locale.GERMAN, "Ranseier").build();
    repo.save(familyName);

    // We change the label now
    familyName.setLabel(new LocalizedText(Locale.GERMAN, "Foobar"));
    FamilyName beforeUpdate = createDeepCopy(familyName);

    updateAndAssertUpdatedLastModifiedTimestamp(familyName);
    assertInDatabaseIsEqualToUpdateable(familyName, beforeUpdate, Function.identity());
  }
}
