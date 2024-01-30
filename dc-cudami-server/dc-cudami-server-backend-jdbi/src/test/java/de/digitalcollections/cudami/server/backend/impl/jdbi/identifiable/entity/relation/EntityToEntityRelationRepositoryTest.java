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
import de.digitalcollections.model.validation.ValidationException;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = EntityToEntityRelationRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@DisplayName("The EntityToEntityRelationRepository")
@Sql(scripts = "classpath:cleanup_database.sql")
public class EntityToEntityRelationRepositoryTest {
  EntityToEntityRelationRepositoryImpl repository;

  @Autowired Jdbi jdbi;
  @Autowired CudamiConfig cudamiConfig;

  @Autowired
  @Qualifier("entityRepositoryImpl")
  private EntityRepositoryImpl<Entity> entityRepository;

  @Autowired private PredicateRepositoryImpl predicateRepository;

  @BeforeEach
  public void beforeEach() {
    repository = new EntityToEntityRelationRepositoryImpl(jdbi, entityRepository, cudamiConfig);
  }

  @Test
  @DisplayName("should save an EntityRelation")
  void saveEntityRelation()
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          RepositoryException,
          ValidationException {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    EntityRelation expectedRelation =
        TestModelFixture.createEntityRelation(
            Map.of(Locale.ENGLISH, "subject entity label"),
            Map.of(Locale.ENGLISH, "object entity label"),
            predicate.getValue());
    expectedRelation.setAdditionalPredicates(List.of("addPred1", "addPred2"));
    entityRepository.save(expectedRelation.getSubject());
    entityRepository.save(expectedRelation.getObject());
    predicateRepository.save(predicate);
    repository.save(List.of(expectedRelation));

    PageResponse<EntityRelation> actual =
        repository.findBySubject(expectedRelation.getSubject(), new PageRequest(0, 10));
    assertThat(actual).hasSize(1);
    EntityRelation actualRelation = actual.getContent().get(0);
    assertThat(actualRelation.getPredicate()).isEqualTo(expectedRelation.getPredicate());
    assertThat(actualRelation.getSubject().getLabel().getText(Locale.ENGLISH))
        .isEqualTo(expectedRelation.getSubject().getLabel().getText(Locale.ENGLISH));
    assertThat(actualRelation.getSubject().getUuid())
        .isEqualTo(expectedRelation.getSubject().getUuid());
    assertThat(actualRelation.getObject().getLabel().getText(Locale.ENGLISH))
        .isEqualTo(expectedRelation.getObject().getLabel().getText(Locale.ENGLISH));
    assertThat(actualRelation.getObject().getUuid())
        .isEqualTo(expectedRelation.getObject().getUuid());
    assertThat(actualRelation.getAdditionalPredicates())
        .isEqualTo(expectedRelation.getAdditionalPredicates());
  }

  @Test
  @DisplayName("should save two EntityRelations and retrieve in same order")
  void saveEntityRelations()
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          RepositoryException,
          ValidationException {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    EntityRelation expectedRelation =
        TestModelFixture.createEntityRelation(
            Map.of(Locale.ENGLISH, "subject entity label"),
            Map.of(Locale.ENGLISH, "object entity label"),
            predicate.getValue());
    expectedRelation.setAdditionalPredicates(List.of("addPred1"));
    entityRepository.save(expectedRelation.getSubject());
    expectedRelation.setSubject(
        entityRepository.getByUuid(expectedRelation.getSubject().getUuid()));
    entityRepository.save(expectedRelation.getObject());
    expectedRelation.setObject(entityRepository.getByUuid(expectedRelation.getObject().getUuid()));
    predicateRepository.save(predicate);

    Predicate predicate2 = new Predicate();
    predicate2.setValue("is_test_2");
    EntityRelation expectedRelation2 =
        new EntityRelation(
            expectedRelation.getSubject(),
            predicate2.getValue(),
            TestModelFixture.createEntity(
                Entity.class, Map.of(Locale.GERMAN, "Label"), Collections.emptyMap()));
    expectedRelation2.setAdditionalPredicates(List.of("addPred2"));
    entityRepository.save(expectedRelation2.getObject());
    expectedRelation2.setObject(
        entityRepository.getByUuid(expectedRelation2.getObject().getUuid()));
    predicateRepository.save(predicate2);

    repository.save(List.of(expectedRelation, expectedRelation2));

    PageResponse<EntityRelation> actual =
        repository.findBySubject(expectedRelation.getSubject(), new PageRequest(0, 10));
    assertThat(actual).hasSize(2);
    assertThat(actual.getContent().get(0)).isEqualTo(expectedRelation);
    assertThat(actual.getContent().get(1)).isEqualTo(expectedRelation2);
  }

  @Test
  @DisplayName("should update the relation if already exists")
  void updateRelations()
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          RepositoryException,
          ValidationException {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    EntityRelation expectedRelation =
        TestModelFixture.createEntityRelation(
            Map.of(Locale.ENGLISH, "subject entity label"),
            Map.of(Locale.ENGLISH, "object entity label"),
            predicate.getValue());
    entityRepository.save(expectedRelation.getSubject());
    entityRepository.save(expectedRelation.getObject());
    expectedRelation =
        new EntityRelation(
            entityRepository.getByUuid(expectedRelation.getSubject().getUuid()),
            predicate.getValue(),
            entityRepository.getByUuid(expectedRelation.getObject().getUuid()));
    expectedRelation.setAdditionalPredicates(List.of("addPred1"));
    predicateRepository.save(predicate);

    repository.save(expectedRelation);

    // now lets change the additional predicate(s)
    expectedRelation.setAdditionalPredicates(List.of("predicate1", "predicate2", "predicate3"));
    repository.save(expectedRelation);

    PageResponse<EntityRelation> actual =
        repository.findBySubject(expectedRelation.getSubject().getUuid(), new PageRequest(0, 10));
    assertThat(actual).hasSize(1);
    EntityRelation actualRelation = actual.getContent().get(0);
    assertThat(actualRelation).isEqualTo(expectedRelation);
    assertThat(actualRelation.getAdditionalPredicates())
        .containsExactly("predicate1", "predicate2", "predicate3");
  }

  @Test
  @DisplayName("returns properly sized pages")
  void testSearchPageSize() throws RepositoryException, ValidationException {
    Predicate predicate = new Predicate();
    predicate.setValue("is_test");
    predicateRepository.save(predicate);

    List<EntityRelation> relations = new ArrayList<>();
    IntStream.range(0, 20)
        .forEach(
            i -> {
              EntityRelation relation;
              try {
                relation =
                    TestModelFixture.createEntityRelation(
                        Map.of(Locale.ENGLISH, "subject entity label " + i),
                        Map.of(Locale.ENGLISH, "object entity label " + i),
                        predicate.getValue());
              } catch (InstantiationException
                  | IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException
                  | NoSuchMethodException
                  | SecurityException e) {
                relation = null;
              }
              relations.add(relation);
            });
    relations.forEach(
        rel -> {
          try {
            entityRepository.save(rel.getSubject());
          } catch (RepositoryException | ValidationException e) {
            throw new RuntimeException(e);
          }
          try {
            entityRepository.save(rel.getObject());
          } catch (RepositoryException | ValidationException e) {
            throw new RuntimeException(e);
          }
        });
    repository.save(relations);

    PageRequest request = new PageRequest(0, 10);
    PageResponse response = repository.find(request);
    List<EntityRelation> content = response.getContent();
    assertThat(content).hasSize(10);
    assertThat(content).containsExactlyElementsOf(relations.subList(0, 10));
  }
}
