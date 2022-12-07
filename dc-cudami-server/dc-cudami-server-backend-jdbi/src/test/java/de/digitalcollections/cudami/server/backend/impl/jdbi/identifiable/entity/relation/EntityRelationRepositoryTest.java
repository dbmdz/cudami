package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.relation.PredicateRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
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
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@DisplayName("The EntityRelationRepository")
public class EntityRelationRepositoryTest {
  EntityRelationRepositoryImpl repository;

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired Jdbi jdbi;
  @Autowired CudamiConfig cudamiConfig;

  @Autowired
  @Qualifier("entityRepositoryImpl")
  private EntityRepositoryImpl<Entity> entityRepository;

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
  void saveEntityRelation()
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException, RepositoryException {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    EntityRelation entityRelation =
        TestModelFixture.createEntityRelation(
            Map.of(Locale.ENGLISH, "subject entity label"),
            Map.of(Locale.ENGLISH, "object entity label"),
            predicate.getValue());
    entityRelation.setAdditionalPredicates(List.of("addPred1", "addPred2"));
    entityRepository.save(entityRelation.getSubject());
    entityRepository.save(entityRelation.getObject());
    predicateRepository.save(predicate);
    repository.save(List.of(entityRelation));

    List<EntityRelation> actual = repository.getBySubject(entityRelation.getSubject());
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getPredicate()).isEqualTo(entityRelation.getPredicate());
    assertThat(actual.get(0).getSubject().getLabel().getText(Locale.ENGLISH))
        .isEqualTo(entityRelation.getSubject().getLabel().getText(Locale.ENGLISH));
    assertThat(actual.get(0).getSubject().getUuid())
        .isEqualTo(entityRelation.getSubject().getUuid());
    assertThat(actual.get(0).getObject().getLabel().getText(Locale.ENGLISH))
        .isEqualTo(entityRelation.getObject().getLabel().getText(Locale.ENGLISH));
    assertThat(actual.get(0).getObject().getUuid()).isEqualTo(entityRelation.getObject().getUuid());
    assertThat(actual.get(0).getAdditionalPredicates())
        .isEqualTo(entityRelation.getAdditionalPredicates());
  }

  @Test
  @DisplayName("should save two EntityRelations and retrieve in same order")
  void saveEntityRelations()
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException, RepositoryException {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    EntityRelation entityRelation =
        TestModelFixture.createEntityRelation(
            Map.of(Locale.ENGLISH, "subject entity label"),
            Map.of(Locale.ENGLISH, "object entity label"),
            predicate.getValue());
    entityRelation.setAdditionalPredicates(List.of("addPred1"));
    entityRepository.save(entityRelation.getSubject());
    entityRelation.setSubject(entityRepository.getByUuid(entityRelation.getSubject().getUuid()));
    entityRepository.save(entityRelation.getObject());
    entityRelation.setObject(entityRepository.getByUuid(entityRelation.getObject().getUuid()));
    predicateRepository.save(predicate);

    Predicate predicate2 = new Predicate();
    predicate2.setValue("is_test_2");
    EntityRelation entityRelation2 =
        new EntityRelation(
            entityRelation.getSubject(),
            predicate2.getValue(),
            TestModelFixture.createEntity(
                Entity.class, Map.of(Locale.GERMAN, "Label"), Collections.emptyMap()));
    entityRelation2.setAdditionalPredicates(List.of("addPred2"));
    entityRepository.save(entityRelation2.getObject());
    entityRelation2.setObject(entityRepository.getByUuid(entityRelation2.getObject().getUuid()));
    predicateRepository.save(predicate2);

    repository.save(List.of(entityRelation, entityRelation2));

    List<EntityRelation> actual = repository.getBySubject(entityRelation.getSubject());
    assertThat(actual).hasSize(2);
    assertThat(actual.get(0)).isEqualTo(entityRelation);
    assertThat(actual.get(1)).isEqualTo(entityRelation2);
  }

  @Test
  @DisplayName("should update the relation if already exists")
  void updateRelations()
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException, RepositoryException {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    EntityRelation entityRelation =
        TestModelFixture.createEntityRelation(
            Map.of(Locale.ENGLISH, "subject entity label"),
            Map.of(Locale.ENGLISH, "object entity label"),
            predicate.getValue());
    entityRepository.save(entityRelation.getSubject());
    entityRepository.save(entityRelation.getObject());
    entityRelation =
        new EntityRelation(
            entityRepository.getByUuid(entityRelation.getSubject().getUuid()),
            predicate.getValue(),
            entityRepository.getByUuid(entityRelation.getObject().getUuid()));
    entityRelation.setAdditionalPredicates(List.of("addPred1"));
    predicateRepository.save(predicate);

    repository.save(entityRelation);

    // now lets change the additional predicate(s)
    entityRelation.setAdditionalPredicates(List.of("predicate1", "predicate2", "predicate3"));
    repository.save(entityRelation);

    List<EntityRelation> actual = repository.getBySubject(entityRelation.getSubject().getUuid());
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0)).isEqualTo(entityRelation);
    assertThat(actual.get(0).getAdditionalPredicates())
        .containsExactly("predicate1", "predicate2", "predicate3");
  }

  @Test
  @DisplayName("returns properly sized pages")
  void testSearchPageSize() throws RepositoryException {
    List<EntityRelation> relations = new ArrayList<>();
    IntStream.range(0, 20)
        .forEach(
            i -> {
              EntityRelation entityRelation;
              try {
                entityRelation =
                    TestModelFixture.createEntityRelation(
                        Map.of(Locale.ENGLISH, "subject entity label " + i),
                        Map.of(Locale.ENGLISH, "object entity label " + i),
                        "is_test");
              } catch (InstantiationException
                  | IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException
                  | NoSuchMethodException
                  | SecurityException e) {
                entityRelation = null;
              }
              relations.add(entityRelation);
            });
    relations.forEach(
        rel -> {
          try {
            entityRepository.save(rel.getSubject());
          } catch (RepositoryException e) {
            throw new RuntimeException(e);
          }
          try {
            entityRepository.save(rel.getObject());
          } catch (RepositoryException e) {
            throw new RuntimeException(e);
          }
        });
    repository.save(relations);

    PageRequest request = new PageRequest(0, 10);
    PageResponse response = repository.find(request);
    List<EntityRelation> content = response.getContent();
    assertThat(content).hasSize(10);
  }
}
