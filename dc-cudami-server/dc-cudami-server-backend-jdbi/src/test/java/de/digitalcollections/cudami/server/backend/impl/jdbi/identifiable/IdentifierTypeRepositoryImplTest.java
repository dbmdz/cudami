package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractRepositoryImplTest;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@DisplayName("The IdentifierType Repository")
class IdentifierTypeRepositoryImplTest extends AbstractRepositoryImplTest {

  IdentifierTypeRepositoryImpl repo;

  @BeforeEach
  public void beforeEach() {
    repo = new IdentifierTypeRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can create correct SQL snippets")
  void providesCorrectSql() throws RepositoryException {
    String sql = repo.getSqlSelectReducedFields();
    assertThat(sql)
        .isEqualTo(
            " idt.uuid idt_uuid, idt.created idt_created, idt.last_modified idt_lastModified, idt.label idt_label, idt.namespace idt_namespace, idt.pattern idt_pattern");
  }

  @Test
  @DisplayName("can save a new identifier type")
  void saveNewIdentifierType() throws RepositoryException, ValidationException {
    IdentifierType actual = new IdentifierType();
    actual.setLabel("type-label");
    actual.setNamespace("type-namespace-" + System.currentTimeMillis()); // Namespace is PK
    actual.setPattern("type-pattern");

    IdentifierType expected = createDeepCopy(actual);

    repo.save(actual);

    assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(expected.getPattern());
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("can retrieve an identifier type by uuid")
  void getByUuid() throws RepositoryException, ValidationException {
    IdentifierType actual = new IdentifierType();
    actual.setLabel("type-label");
    actual.setNamespace("type-namespace-" + System.currentTimeMillis());
    actual.setPattern("type-pattern");

    IdentifierType expected = createDeepCopy(actual);

    repo.save(actual);

    actual = repo.getByUuid(actual.getUuid());
    assertThat(actual).isNotNull();
    assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(expected.getPattern());
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("returns null when no identifier type by uuid was found")
  void getByUuidNotFound() throws RepositoryException, ValidationException {
    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace("type-namespace-" + System.currentTimeMillis());
    expected.setPattern("type-pattern");
    repo.save(expected);

    IdentifierType actual = repo.getByUuid(UUID.randomUUID());
    assertThat(actual).isNull();
  }

  @Test
  @DisplayName("can retrieve an identifier type by namespace")
  void getByNamespace() throws RepositoryException, ValidationException {
    String namespace = "type-namespace-" + System.currentTimeMillis();

    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace(namespace);
    expected.setPattern("type-pattern");

    repo.save(expected);

    IdentifierType actual = repo.getByNamespace(namespace);
    assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(expected.getPattern());
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("returns null with no identifier type by namespace was found")
  void getByNamespaceNotFound() throws RepositoryException, ValidationException {
    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace("type-namespace-" + System.currentTimeMillis());
    expected.setPattern("type-pattern");
    repo.save(expected);

    IdentifierType actual = repo.getByNamespace("nonexistant");
    assertThat(actual).isNull();
  }

  @Test
  @DisplayName("can update an identifier type")
  void update() throws RepositoryException, ValidationException {
    IdentifierType initial = new IdentifierType();
    initial.setLabel("type-label");
    initial.setNamespace("type-namespace-" + System.currentTimeMillis());
    initial.setPattern("type-pattern");

    repo.save(initial);
    initial.setLabel("otherlabel");
    initial.setPattern("otherpattern");
    initial.setNamespace("othernamespace-" + System.currentTimeMillis());

    IdentifierType beforeUpdate = createDeepCopy(initial);

    repo.update(initial);
    assertThat(initial.getLabel()).isEqualTo(beforeUpdate.getLabel());
    assertThat(initial.getNamespace()).isEqualTo(beforeUpdate.getNamespace());
    assertThat(initial.getPattern()).isEqualTo(beforeUpdate.getPattern());
  }

  @Test
  @DisplayName("can delete an identifier type")
  void delete() throws RepositoryException, ValidationException {
    IdentifierType initial = new IdentifierType();
    initial.setLabel("type-label");
    initial.setNamespace("type-namespace-" + System.currentTimeMillis());
    initial.setPattern("type-pattern");

    repo.save(initial);

    repo.deleteByUuid(initial.getUuid());

    IdentifierType actual = repo.getByUuid(initial.getUuid());
    assertThat(actual).isNull();

    initial.setLabel("otherlabel");
    initial.setPattern("otherpattern");
    initial.setNamespace("othernamespace-" + System.currentTimeMillis());
  }

  @Test
  @DisplayName("can find identifier types")
  void find() throws RepositoryException, ValidationException {
    // Insert two identifier types
    String namespace1 = "type-namespace-" + System.currentTimeMillis();
    String namespace2 = "type-namespace-" + (System.currentTimeMillis() + 1);

    IdentifierType type1 = new IdentifierType();
    type1.setLabel("type-label-1");
    type1.setNamespace(namespace1);
    type1.setPattern("type-pattern-1");
    repo.save(type1);

    IdentifierType type2 = new IdentifierType();
    type2.setLabel("type-label-2");
    type2.setNamespace(namespace2);
    type2.setPattern("type-pattern-2");
    repo.save(type2);

    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("namespace").contains(namespace1).build())
            .build();
    PageRequest pageRequest =
        PageRequest.builder().pageNumber(0).pageSize(99).filtering(filtering).build();
    PageResponse<IdentifierType> pageResponse = repo.find(pageRequest);
    List<IdentifierType> actualContent = pageResponse.getContent();
    assertThat(actualContent).hasSize(1);
    IdentifierType actual = actualContent.get(0);
    assertThat(actual.getLabel()).isEqualTo(type1.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(type1.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(type1.getPattern());
  }
}
