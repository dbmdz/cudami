package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = FamilyNameRepositoryImpl.class)
@DisplayName("The FamiliyName Repository")
class FamilyNameRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<FamilyNameRepositoryImpl> {

  @BeforeEach
  public void beforeEach() {
    repo = new FamilyNameRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can save a FamilyName and fill uuid and timestamps")
  void testSave() throws RepositoryException {
    FamilyName familyName = FamilyName.builder().label(Locale.GERMAN, "Ranseier").build();
    saveAndAssertTimestampsAndEqualityToSaveable(familyName);
  }

  @Test
  @DisplayName("can update and modify lastModified timestamp")
  void testUpdate() throws RepositoryException {
    FamilyName familyName = FamilyName.builder().label(Locale.GERMAN, "Ranseier").build();
    repo.save(familyName);

    // We change the label now
    familyName.setLabel(new LocalizedText(Locale.GERMAN, "Foobar"));
    FamilyName beforeUpdate = createDeepCopy(familyName);

    updateAndAssertUpdatedLastModifiedTimestamp(familyName);
    assertInDatabaseIsEqualToUpdateable(familyName, beforeUpdate, Function.identity());
  }
}
