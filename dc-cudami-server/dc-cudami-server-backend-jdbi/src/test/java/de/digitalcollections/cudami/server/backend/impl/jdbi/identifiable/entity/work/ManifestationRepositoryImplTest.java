package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.HumanSettlementRepository;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.manifestation.Title;
import de.digitalcollections.model.identifiable.entity.manifestation.TitleType;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManifestationRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@DisplayName("The Manifestation Repository")
@Sql(scripts = "classpath:cleanup_database.sql")
class ManifestationRepositoryImplTest {

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired ManifestationRepositoryImpl repo;

  @Autowired CorporateBodyRepository corporateBodyRepository;
  @Autowired HumanSettlementRepository humanSettlementRepository;
  @Autowired PredicateRepository predicateRepository;

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  @Disabled("work in progress!")
  void testSaveManifestationMapOfStringObject() throws RepositoryException {
    CorporateBody editor = CorporateBody.builder().label("Editor").addName("Editor").build();
    editor = corporateBodyRepository.save(editor);
    CorporateBody someoneElse =
        CorporateBody.builder().label("Someone else").addName("Someone else").build();
    someoneElse = corporateBodyRepository.save(someoneElse);

    Predicate isEditorOf =
        predicateRepository.save(Predicate.builder().value("is_editor_of").build());
    Predicate isSomethingElseOf =
        predicateRepository.save(Predicate.builder().value("is_somethingelse_of").build());

    Manifestation manifestation =
        Manifestation.builder()
            .label(Locale.GERMAN, "ein Label")
            .composition("composition")
            .expressionType(ExpressionType.builder().mainType("BOOK").subType("PRINT").build())
            .language(Locale.GERMAN)
            .mediaType("BOOK")
            //            .publisher(publisher)
            .title(
                Title.builder()
                    .text(new LocalizedText(Locale.GERMAN, "Ein deutscher Titel"))
                    .titleType(new TitleType("main", "main"))
                    .textLocaleOfOriginalScript(Locale.GERMAN)
                    .textLocaleOfOriginalScript(Locale.ENGLISH)
                    .build())
            .title(
                Title.builder()
                    .text(new LocalizedText(Locale.GERMAN, "Untertitel"))
                    .titleType(new TitleType("main", "sub"))
                    .textLocaleOfOriginalScript(Locale.GERMAN)
                    .build())
            .build();
    manifestation.addRelation(new EntityRelation(editor, "is_editor_of", manifestation));
    manifestation.addRelation(
        new EntityRelation(someoneElse, "is_somethingelse_of", manifestation));
    Manifestation saved = repo.save(manifestation);

    // we add the relations manually, actually done by the service
    // TODO

    Manifestation actual = repo.getByUuid(saved.getUuid());
    // assertThat(actual).isEqualTo(saved);
  }

  @Test
  void testUpdateManifestationMapOfStringObject() {}
}
