package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation.EntityToEntityRelationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.text.TitleType;
import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.time.TimeValue;
import de.digitalcollections.model.validation.ValidationException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = WorkRepositoryImpl.class)
@DisplayName("The Work Repository")
class WorkRepositoryImplTest extends AbstractIdentifiableRepositoryImplTest<WorkRepositoryImpl> {

  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;
  @Autowired HumanSettlementRepositoryImpl humanSettlementRepository;
  @Autowired LocalDateRangeMapper localDateRangeMapper;
  @Autowired TitleMapper titleMapper;
  @Autowired EntityRepositoryImpl<Entity> entityRepository;
  @Autowired AgentRepositoryImpl<Agent> agentRepository;
  @Autowired SubjectRepository subjectRepository;
  @Autowired PersonRepositoryImpl personRepository;
  @Autowired EntityToEntityRelationRepositoryImpl entityRelationRepository;
  @Autowired ItemRepositoryImpl itemRepository;
  @Autowired ManifestationRepositoryImpl manifestationRepository;
  @Autowired PredicateRepository predicateRepository;

  @BeforeEach
  void beforeEach() {
    repo =
        new WorkRepositoryImpl(
            jdbi,
            cudamiConfig,
            identifierRepository,
            urlAliasRepository,
            localDateRangeMapper,
            titleMapper,
            entityRepository,
            agentRepository,
            humanSettlementRepository,
            manifestationRepository,
            itemRepository,
            personRepository,
            entityRelationRepository);
  }

  @DisplayName("Returns null when retrieve by uuid finds no match")
  @Test
  public void noRetrieveByUuid() throws RepositoryException {
    assertThat(repo.getByUuid(UUID.randomUUID())).isNull();
  }

  @DisplayName("Returns null when retrieve by identifier finds no match")
  @Test
  public void noRetrieveByIdentifier() throws RepositoryException {
    assertThat(
            repo.getByIdentifier(Identifier.builder().namespace("gnd").id("1234-5678-9").build()))
        .isNull();
  }

  @DisplayName("fills and returns the UUID on a saved work")
  @Test
  public void saveFillsAndReturnsUuid() throws RepositoryException, ValidationException {
    Work actual = Work.builder().label(Locale.GERMAN, "Erstlingswerk").build();
    repo.save(actual);
    assertThat(actual.getUuid()).isNotNull();
  }

  @DisplayName("persists all fields of a saved work")
  @Test
  public void persistAllFieldsOnSave() throws RepositoryException, ValidationException {
    Work parentWork =
        Work.builder()
            .label(Locale.GERMAN, "Parent")
            .identifier(Identifier.builder().namespace("parent").id("work").build())
            .build();
    repo.save(parentWork);

    Work work =
        Work.builder()
            .label(Locale.GERMAN, "Erstlingswerk")
            .titles(
                List.of(
                    Title.builder()
                        .titleType(new TitleType("uni", "main"))
                        .text(new LocalizedText(Locale.GERMAN, "Erstlingswerk"))
                        .build(),
                    Title.builder()
                        .titleType(new TitleType("uni", "sub"))
                        .text(new LocalizedText(Locale.GERMAN, "Aller Anfang ist schwer"))
                        .build()))
            .creationDateRange(
                new LocalDateRange(LocalDate.parse("-1000-01-01"), LocalDate.parse("2023-02-01")))
            .creationTimeValue(new TimeValue(2023, (byte) 1, (byte) 1))
            .firstAppearedDate(LocalDate.parse("2023-02-02"))
            .firstAppearedDatePresentation("02.02.2023")
            .firstAppearedTimeValue(new TimeValue(2023, (byte) 1))
            .parents(List.of(parentWork))
            .build();
    saveAndAssertTimestampsAndEqualityToSaveable(work);
  }

  @DisplayName("can update all fields of a work")
  @Test
  public void testUpdate() throws RepositoryException, ValidationException {
    Work parentWork1 = Work.builder().label(Locale.GERMAN, "Parent").build();
    repo.save(parentWork1);

    Work parentWork2 = Work.builder().label(Locale.GERMAN, "zweiter Parent").build();
    repo.save(parentWork2);

    Subject subject1 =
        Subject.builder()
            .subjectType("Test")
            .label(new LocalizedText(Locale.GERMAN, "Test"))
            .build();
    Subject subject2 =
        Subject.builder()
            .subjectType("Test")
            .identifier(Identifier.builder().namespace("foo").id("bar").build())
            .build();
    Subject subject3 =
        Subject.builder()
            .subjectType("Test")
            .identifier(Identifier.builder().namespace("bla").id("baz").build())
            .build();
    subjectRepository.save(subject1);
    subjectRepository.save(subject2);
    subjectRepository.save(subject3);

    Work work =
        Work.builder()
            .label(new LocalizedText(Locale.GERMAN, "veraltetes Werk"))
            .title(
                Title.builder()
                    .titleType(new TitleType("uni", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "altes Erstlingswerk"))
                    .build())
            .subjects(new HashSet<>(Arrays.asList(subject1)))
            .creationDateRange(
                new LocalDateRange(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-02-01")))
            .creationTimeValue(new TimeValue(2022, (byte) 1, (byte) 1))
            .firstAppearedDate(LocalDate.parse("2022-02-02"))
            .firstAppearedDatePresentation("02.02.2022")
            .firstAppearedTimeValue(new TimeValue(2022, (byte) 1))
            .parents(List.of(parentWork1))
            .build();
    repo.save(work);

    work.setLabel(new LocalizedText(Locale.GERMAN, "aktualisiertes Werk"));
    work.setTitles(
        List.of(
            Title.builder()
                .titleType(new TitleType("uni", "main"))
                .text(new LocalizedText(Locale.GERMAN, "altes Erstlingswerk"))
                .build()));
    work.setSubjects(new HashSet<>(Arrays.asList(subject2, subject3)));
    work.setCreationDateRange(
        new LocalDateRange(LocalDate.parse("2023-01-01"), LocalDate.parse("2023-02-01")));
    work.setCreationTimeValue(new TimeValue(2023, (byte) 1, (byte) 1));
    work.setFirstAppearedDate(LocalDate.parse("2023-02-02"));
    work.setFirstAppearedDatePresentation("02.02.2023");
    work.setFirstAppearedTimeValue(new TimeValue(2022, (byte) 1));
    work.setParents(List.of(parentWork1, parentWork2));

    updateAndAssertUpdatedLastModifiedTimestamp(work);
  }

  @DisplayName(
      "can return null for getByItemUuid, when no item is connected to a manifestation and a work")
  @Test
  public void getByItemUuidReturnsNull() throws RepositoryException, ValidationException {
    // First test: Query for nonexisting item must return null
    assertThat(repo.getByItem(UUID.randomUUID())).isNull();

    // Second test: Query for existing item with no connection to a
    // manifestation must return null;
    Item item = Item.builder().label(Locale.GERMAN, "Item").build();
    itemRepository.save(item);
    assertThat(repo.getByItem(item.getUuid())).isNull();
  }

  @DisplayName("can return the work, connected to an item")
  @Test
  public void getByItemUuidReturnsWork() throws RepositoryException, ValidationException {
    Work work = Work.builder().label(Locale.GERMAN, "Erstlingswerk").build();
    repo.save(work);

    Manifestation manifestation =
        Manifestation.builder()
            .work(work)
            .label(Locale.GERMAN, "Erstausgabe")
            .titles(
                List.of(
                    Title.builder()
                        .titleType(new TitleType("MAIN", "MAIN"))
                        .text(new LocalizedText(Locale.GERMAN, "Erstausgabe"))
                        .build()))
            .build();
    manifestationRepository.save(manifestation);

    // First test: Item with no manifestation must not return any work
    Item item = Item.builder().label(Locale.GERMAN, "Erstexemplar").build();
    itemRepository.save(item);
    assertThat(repo.getByItem(item.getUuid())).isNull();

    // Second test: Item with existing manifestation->work chain must return the work
    item.setManifestation(manifestation);
    itemRepository.update(item);
    Work actual = repo.getByItem(item.getUuid());
    assertThat(actual).isEqualTo(work);
  }

  @DisplayName(
      "can return an empty set for getByPersonUuid, when no persons are connected with a work")
  @Test
  public void getByPersonUuidReturnsEmptySet() throws RepositoryException, ValidationException {
    // First test: Query for nonexisting person must return null
    assertThat(repo.getByPerson(UUID.randomUUID())).isEmpty();

    // Second test: Query for existing person with no connection to a
    // work must return null;
    Person person =
        Person.builder()
            .label(Locale.GERMAN, "Karl Ranseier")
            .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .build();
    personRepository.save(person);

    assertThat(repo.getByPerson(person.getUuid())).isEmpty();
  }

  @DisplayName("can return the set of connected persons for a work")
  @Test
  public void getByPersonUuidReturnsSet() throws RepositoryException, ValidationException {
    Person person =
        Person.builder()
            .label(Locale.GERMAN, "Karl Ranseier")
            .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .build();
    personRepository.save(person);

    Predicate predicate = Predicate.builder().value("is_creator_of").build();
    predicateRepository.save(predicate);

    EntityRelation entityRelation =
        EntityRelation.builder().subject(person).predicate(predicate.getValue()).build();
    Work work =
        Work.builder()
            .label(Locale.GERMAN, "Erstlingswerk")
            .relations(List.of(entityRelation))
            .build();
    repo.save(work);

    // Since we use the repository and not the service, we have to
    // persist the relations manually
    entityRelation.setObject(work);
    entityRelationRepository.save(entityRelation);
    entityRelation.setObject(null); // to avoid recursion

    Set<Work> actual = repo.getByPerson(person.getUuid());

    assertThat(actual).containsExactly(work);
  }
}
