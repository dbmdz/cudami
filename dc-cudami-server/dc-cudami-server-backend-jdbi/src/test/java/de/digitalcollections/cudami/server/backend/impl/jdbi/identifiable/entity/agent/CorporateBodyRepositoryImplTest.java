package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Locale;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {CorporateBodyRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The CorporateBody Repository")
class CorporateBodyRepositoryImplTest {

  CorporateBodyRepositoryImpl repo;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo = new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can save a CorporateBody")
  public void saveCorporateBody() {
    CorporateBody creator =
        CorporateBody.builder()
            .label(Locale.GERMAN, "Körperschaft")
            .label(Locale.ENGLISH, "Corporate Body")
            .build();

    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
    CorporateBody actual = corporateBodyRepository.save(creator);

    assertThat(actual.getUuid()).isNotNull();
    assertThat(actual.getLabel().getLocales()).containsExactly(Locale.GERMAN, Locale.ENGLISH);
  }

  @Test
  @DisplayName("can retrieve a CorporateBody")
  public void saveAndRetrieveCorporateBody() {
    CorporateBody creator =
        CorporateBody.builder()
            .label(Locale.GERMAN, "Körperschaft")
            .label(Locale.ENGLISH, "Corporate Body")
            .build();

    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
    CorporateBody persisted = corporateBodyRepository.save(creator);

    CorporateBody actual = corporateBodyRepository.getByUuid(persisted.getUuid());
    assertThat(actual).isEqualTo(persisted);
  }

  @Test
  @DisplayName("can retrieve a CorporateBody by name")
  public void findCorporateBodyByName() {
    CorporateBody creator =
        CorporateBody.builder()
            .label(Locale.GERMAN, "Körperschaft")
            .label(Locale.ENGLISH, "Corporate Body")
            .addName(Locale.GERMAN, "Ein Unternehmen (Körperschaft)")
            .addName(Locale.ENGLISH, "A corporation doing funny stuff")
            .build();

    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
    CorporateBody persisted = corporateBodyRepository.save(creator);
    PageResponse<CorporateBody> byName =
        corporateBodyRepository.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(10)
                .filtering(
                    Filtering.builder()
                        .add(
                            new FilterCriterion<>(
                                "name", FilterOperation.CONTAINS, "Unternehmen Körperschaft"))
                        .build())
                .build());
    PageResponse<CorporateBody> byGermanName =
        corporateBodyRepository.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(10)
                .filtering(
                    Filtering.builder()
                        .add(
                            new FilterCriterion<>(
                                "name.de", FilterOperation.CONTAINS, "Unternehmen Körperschaft"))
                        .build())
                .build());
    PageResponse<CorporateBody> byEnglishName =
        corporateBodyRepository.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(10)
                .filtering(
                    Filtering.builder()
                        .add(
                            new FilterCriterion<>(
                                "name.en", FilterOperation.CONTAINS, "corporation funny stuff"))
                        .build())
                .build());
    PageResponse<CorporateBody> byEnglishNameEquals =
        corporateBodyRepository.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(10)
                .filtering(
                    Filtering.builder()
                        .add(
                            new FilterCriterion<>(
                                "name.en",
                                FilterOperation.EQUALS,
                                "a corporation doing funny stuff"))
                        .build())
                .build());
    PageResponse<CorporateBody> noMatch =
        corporateBodyRepository.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(10)
                .filtering(
                    Filtering.builder()
                        .add(
                            new FilterCriterion<>(
                                "name", FilterOperation.CONTAINS, "should not match"))
                        .build())
                .build());

    assertThat(byName.getNumberOfElements()).isEqualTo(1);
    assertThat(byName.getContent().get(0)).isEqualTo(persisted);
    assertThat(byGermanName.getNumberOfElements()).isEqualTo(1);
    assertThat(byGermanName.getContent().get(0)).isEqualTo(persisted);
    assertThat(byEnglishName.getNumberOfElements()).isEqualTo(1);
    assertThat(byEnglishName.getContent().get(0)).isEqualTo(persisted);
    assertThat(byEnglishNameEquals.getNumberOfElements()).isEqualTo(1);
    assertThat(byEnglishNameEquals.getContent().get(0)).isEqualTo(persisted);
    assertThat(noMatch.getNumberOfElements()).isEqualTo(0);
    assertThat(noMatch.getContent()).isEmpty();
  }
}
