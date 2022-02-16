package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.relation.PredicateRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = EntityRelationRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The EntityRelationRepository")
public class EntityRelationRepositoryTest {
  EntityRelationRepositoryImpl repository;

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired Jdbi jdbi;
  @Autowired CudamiConfig cudamiConfig;

  @Autowired
  @Qualifier("entityRepositoryImpl")
  private EntityRepositoryImpl entityRepository;

  @Autowired private PredicateRepositoryImpl predicateRepository;

  @BeforeEach
  public void beforeEach() {
    repository = new EntityRelationRepositoryImpl(jdbi, entityRepository, cudamiConfig);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  @DisplayName("should save an EntityRelation")
  void saveEntityRelation() {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    EntityRelation entityRelation =
        TestModelFixture.createEntityRelation(
            Map.of(Locale.ENGLISH, "subject entity label"),
            Map.of(Locale.ENGLISH, "object entity label"),
            predicate.getValue());
    predicateRepository.save(predicate);
    List<EntityRelation> actual = repository.save(List.of(entityRelation));

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getPredicate()).isEqualTo(entityRelation.getPredicate());
    assertThat(actual.get(0).getSubject().getLabel().getText(Locale.ENGLISH))
        .isEqualTo(entityRelation.getSubject().getLabel().getText(Locale.ENGLISH));
    assertThat(actual.get(0).getSubject().getUuid())
        .isEqualTo(entityRelation.getSubject().getUuid());
    assertThat(actual.get(0).getObject().getLabel().getText(Locale.ENGLISH))
        .isEqualTo(entityRelation.getObject().getLabel().getText(Locale.ENGLISH));
    assertThat(actual.get(0).getObject().getUuid()).isEqualTo(entityRelation.getObject().getUuid());
  }

  @Test
  @DisplayName("returns properly sized pages")
  void testSearchPageSize() {
    List<EntityRelation> relations = new ArrayList<>();
    IntStream.range(0, 20)
        .forEach(
            i -> {
              EntityRelation entityRelation =
                  TestModelFixture.createEntityRelation(
                      Map.of(Locale.ENGLISH, "subject entity label " + i),
                      Map.of(Locale.ENGLISH, "object entity label " + i),
                      "is_test");
              relations.add(entityRelation);
            });
    repository.save(relations);

    PageRequest request = new PageRequest(0, 10);
    PageResponse response = repository.find(request);
    List<EntityRelation> content = response.getContent();
    assertThat(content).hasSize(10);
  }
}
