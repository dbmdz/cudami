package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.HumanSettlementRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.identifiable.entity.work.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.identifiable.entity.work.Title;
import de.digitalcollections.model.identifiable.entity.work.TitleType;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.time.LocalDateRange;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  void testSaveManifestationMapOfStringObject() {
    CorporateBody publisher =
        CorporateBody.builder().label("Publisher").addName("Publisher").build();
    publisher = corporateBodyRepository.save(publisher);

    HumanSettlement publicationLocation =
        HumanSettlement.builder()
            .name(new LocalizedText(Locale.GERMAN, "München"))
            .label("München")
            .build();
    publicationLocation = humanSettlementRepository.save(publicationLocation);

    Manifestation manifestation =
        Manifestation.builder()
            .label(Locale.GERMAN, "ein Label")
            .composition("composition")
            .expressionType(ExpressionType.builder().mainType("BOOK").subType("PRINT").build())
            .language(Locale.GERMAN)
            .mediaType("BOOK")
            .publication(
                Publisher.builder()
                    .agent(List.of(publisher))
                    .locations(List.of(publicationLocation))
                    .build())
            .publishingDateRange(new LocalDateRange(LocalDate.of(2020, 1, 15), LocalDate.now()))
            .title(
                Title.builder()
                    .text(new LocalizedText(Locale.GERMAN, "Ein deutscher Titel"))
                    .titleType(new TitleType("main", "main"))
                    .textLocalesOfOriginalScripts(Set.of(Locale.GERMAN, Locale.ENGLISH))
                    .build())
            .build();
    Manifestation saved = repo.save(manifestation);
  }

  @Test
  void testUpdateManifestationMapOfStringObject() {}
}
