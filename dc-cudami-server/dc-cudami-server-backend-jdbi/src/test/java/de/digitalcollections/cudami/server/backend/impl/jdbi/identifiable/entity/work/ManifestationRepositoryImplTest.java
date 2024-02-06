package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityToEntityRelationRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.ExpressionTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.manifestation.ProductionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.text.TitleType;
import de.digitalcollections.model.text.contentblock.Text;
import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.validation.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = ManifestationRepositoryImpl.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("The Manifestation Repository")
class ManifestationRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<ManifestationRepositoryImpl> {

  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;
  @Autowired CorporateBodyRepositoryImpl corporateBodyRepository;
  @Autowired HumanSettlementRepositoryImpl humanSettlementRepository;
  @Autowired PredicateRepository predicateRepository;
  @Autowired EntityToEntityRelationRepository entityRelationRepository;
  @Autowired SubjectRepository subjectRepository;
  @Autowired ExpressionTypeMapper expressionTypeMapper;
  @Autowired LocalDateRangeMapper localDateRangeMapper;
  @Autowired TitleMapper titleMapper;
  @Autowired EntityRepositoryImpl<Entity> entityRepository;
  @Autowired PersonRepositoryImpl personRepository;
  @Autowired AgentRepositoryImpl<Agent> agentRepository;
  @Autowired WorkRepositoryImpl workRepository;

  @BeforeEach
  void beforeEach() {
    repo =
        new ManifestationRepositoryImpl(
            jdbi,
            cudamiConfig,
            identifierRepository,
            urlAliasRepository,
            expressionTypeMapper,
            localDateRangeMapper,
            titleMapper,
            entityRepository,
            agentRepository,
            humanSettlementRepository);
  }

  @Test
  @DisplayName("1.0. Save a manifestation with parent")
  void testSaveManifestation() throws RepositoryException, ValidationException {
    // agents for relations
    CorporateBody editor =
        CorporateBody.builder()
            .label("Editor")
            .addName("Editor")
            .identifier(Identifier.builder().namespace("namespace").id("editor").build())
            .build();
    corporateBodyRepository.save(editor);
    CorporateBody someoneElse =
        CorporateBody.builder()
            .label("Someone else")
            .addName("Someone else")
            .identifier(Identifier.builder().namespace("namespace").id("someoneElse").build())
            .build();
    corporateBodyRepository.save(someoneElse);

    // predicates
    Predicate isEditorOf = Predicate.builder().value("is_editor_of").build();
    predicateRepository.save(isEditorOf);
    Predicate isSomethingElseOf = Predicate.builder().value("is_somethingelse_of").build();
    predicateRepository.save(isSomethingElseOf);

    // subjects
    Subject subject = ensurePersistedSubject();

    // parent
    Manifestation parent = ensurePersistedParentManifestation();

    List<Title> titles = prepareTitles();

    Work work =
        Work.builder()
            .label(new LocalizedText(Locale.forLanguageTag("en-Latn"), "A referenced work"))
            .label(new LocalizedText(Locale.forLanguageTag("de-Latn"), "Ein Werk"))
            .description(Locale.forLanguageTag("en-Latn"), "something...")
            .build();
    workRepository.save(work);

    Manifestation manifestation = prepareManifestation(subject, parent, titles);
    manifestation.addRelation(new EntityRelation(editor, "is_editor_of", manifestation));
    manifestation.addRelation(
        EntityRelation.builder()
            .subject(someoneElse)
            .predicate("is_somethingelse_of")
            .object(manifestation)
            .additionalPredicate("additional predicate")
            .build());
    manifestation.setWork(work);
    repo.save(manifestation);

    // we add the relations manually, actually done by the service
    entityRelationRepository.save(manifestation.getRelations());

    Manifestation actual = repo.getByUuid(manifestation.getUuid());

    assertThat(actual.getTitles()).isEqualTo(titles);
    assertThat(actual.getExpressionTypes()).isEqualTo(manifestation.getExpressionTypes());

    assertThat(actual.getRelations()).size().isEqualTo(2);
    assertThat(actual.getRelations().get(0))
        .isEqualTo(new EntityRelation(editor, "is_editor_of", null));
    assertThat(actual.getRelations().get(1))
        .isEqualTo(
            EntityRelation.builder()
                .subject(someoneElse)
                .predicate("is_somethingelse_of")
                .additionalPredicate("additional predicate")
                .build());

    assertThat(actual.getSubjects()).containsExactlyInAnyOrder(subject);
    assertThat(actual.getParents())
        .containsExactlyInAnyOrder(
            new RelationSpecification<Manifestation>("The child's title", null, parent));
    assertThat(manifestation.getProductionInfo().getPublishers())
        .allSatisfy(publisher -> assertThat(publisher.getAgent().getUuid()).isNotNull());
    assertThat(manifestation.getPublicationInfo().getPublishers())
        .allSatisfy(publisher -> assertThat(publisher.getAgent().getUuid()).isNotNull());
    assertThat(actual.getProductionInfo()).isEqualTo(manifestation.getProductionInfo());
    assertThat(actual.getPublicationInfo()).isEqualTo(manifestation.getPublicationInfo());
    assertThat(actual.getPublicationInfo().getPublishers()).size().isEqualTo(1);
    assertThat(actual.getPublicationInfo().getPublishers().get(0).getAgent())
        .isExactlyInstanceOf(CorporateBody.class);
    assertThat(actual.getProductionInfo().getPublishers()).size().isEqualTo(1);
    assertThat(actual.getProductionInfo().getPublishers().get(0).getAgent())
        .isExactlyInstanceOf(Person.class);
    // received work only contains uuid and label so we create a new work here prior to comparison
    Work containedWork =
        Work.builder()
            .label(new LocalizedText(Locale.forLanguageTag("en-Latn"), "A referenced work"))
            .label(new LocalizedText(Locale.forLanguageTag("de-Latn"), "Ein Werk"))
            .uuid(work.getUuid())
            .build();
    assertThat(actual.getWork()).isEqualTo(containedWork);
  }

  @Test
  @DisplayName("1.1. Update a manifestation")
  void testUpdateManifestation() throws RepositoryException, ValidationException {
    Subject subject = ensurePersistedSubject();
    Manifestation parent = ensurePersistedParentManifestation();
    List<Title> titles = prepareTitles();
    Manifestation manifestation = prepareManifestation(subject, parent, titles);
    repo.save(manifestation);

    Manifestation persisted = repo.getByUuid(manifestation.getUuid());
    manifestation.getLabel().put(Locale.ENGLISH, "An updated label");
    manifestation
        .getTitles()
        .add(
            Title.builder()
                .text(new LocalizedText(Locale.ENGLISH, "An updated Title"))
                .titleType(new TitleType("MAIN", "MAIN"))
                .build());
    manifestation.setParents(null);
    manifestation.setNavDate(LocalDate.now());
    repo.update(manifestation);

    var actual = repo.getByUuid(manifestation.getUuid());
    assertThat(actual.getLabel()).isEqualTo(manifestation.getLabel());
    assertThat(actual.getTitles()).size().isEqualTo(5);
    assertThat(actual.getTitles()).isEqualTo(manifestation.getTitles());
    assertThat(actual.getParents()).isEmpty();
    assertThat(actual.getNavDate()).isNotNull();
    assertThat(actual.getNavDate()).isEqualTo(manifestation.getNavDate());
  }

  @Test
  @DisplayName("2.0. Find any manifestation")
  void testFind() throws RepositoryException, ValidationException {
    Subject subject = ensurePersistedSubject();
    Manifestation parent = ensurePersistedParentManifestation();
    List<Title> titles = prepareTitles();
    Manifestation manifestation = prepareManifestation(subject, parent, titles);
    repo.save(manifestation);

    PageResponse<Manifestation> actual = repo.find(new PageRequest(0, 10));
    assertThat(actual.getTotalElements()).isEqualTo(2);
    assertThat(actual.getContent()).size().isEqualTo(2);
    assertThat(actual.getContent().stream().map(m -> m.getUuid()).toList())
        .contains(manifestation.getUuid(), parent.getUuid());
  }

  @Test
  @DisplayName("can find children")
  public void findChildren() throws RepositoryException, ValidationException {
    Manifestation parent =
        Manifestation.builder()
            .label(Locale.GERMAN, "Parent")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "Parent"))
                    .build())
            .build();
    repo.save(parent);

    Manifestation child1 =
        Manifestation.builder()
            .label(Locale.GERMAN, "Child 1")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "Child 1"))
                    .build())
            .parent(
                RelationSpecification.<Manifestation>builder()
                    .sortKey("sortkey-1")
                    .title("title 1")
                    .subject(parent)
                    .build())
            .build();
    repo.save(child1);

    Manifestation child2 =
        Manifestation.builder()
            .label(Locale.GERMAN, "Child 2")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "Child 2"))
                    .build())
            .parent(
                RelationSpecification.<Manifestation>builder()
                    .sortKey("sortkey-2")
                    .title("title 2")
                    .subject(parent)
                    .build())
            .build();
    repo.save(child2);

    PageResponse<Manifestation> actual =
        repo.findSubParts(parent.getUuid(), new PageRequest(0, 10));
    assertThat(actual.getContent()).containsExactlyInAnyOrder(child1, child2);
  }

  @Test
  @DisplayName("can return the languages of manifestations for a work")
  public void languagesForManifestationsOfWork() throws RepositoryException, ValidationException {
    Work work = Work.builder().label(Locale.GERMAN, "Werk").build();
    workRepository.save(work);
    LocalizedText labelText = new LocalizedText();
    labelText.setText(Locale.GERMAN, "Manifestation");
    labelText.setText(Locale.ITALIAN, "manifestatione");
    Manifestation manifestation =
        Manifestation.builder()
            .label(labelText)
            .title(Title.builder().titleType(new TitleType("main", "main")).text(labelText).build())
            .work(work)
            .build();
    repo.save(manifestation);

    List<Locale> actual = repo.getLanguagesOfManifestationsForWork(work.getUuid());
    assertThat(actual).containsExactlyInAnyOrder(Locale.GERMAN, Locale.ITALIAN);
  }

  @Test
  @DisplayName("can find manifestations for a work")
  public void findManifestationsForWork() throws RepositoryException, ValidationException {
    Work work = Work.builder().label(Locale.GERMAN, "Werk").build();
    workRepository.save(work);

    Manifestation manifestation1 =
        Manifestation.builder()
            .label(Locale.GERMAN, "Test 1")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "Test 1"))
                    .build())
            .work(work)
            .build();
    repo.save(manifestation1);

    Manifestation manifestation2 =
        Manifestation.builder()
            .label(Locale.GERMAN, "Test 2")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "Test 2"))
                    .build())
            .work(work)
            .build();
    repo.save(manifestation2);

    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    PageResponse<Manifestation> actualPageResponse =
        repo.findManifestationsByWork(work.getUuid(), pageRequest);

    // For verification, we only check the UUID of the embedded work, not any other contents of the
    // work
    List<Manifestation> actual =
        actualPageResponse.getContent().stream()
            .peek(m -> m.setWork(Work.builder().uuid(m.getWork().getUuid()).build()))
            .toList();
    List<Manifestation> expected =
        Stream.of(manifestation1, manifestation2)
            .peek(m -> m.setWork(Work.builder().uuid(m.getWork().getUuid()).build()))
            .toList();

    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @DisplayName("can remove a parent manifestation from a manifestation")
  @Test
  public void removeParentManifestation() throws RepositoryException, ValidationException {
    Manifestation parent =
        Manifestation.builder()
            .label(Locale.GERMAN, "Parent")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "Parent"))
                    .build())
            .build();
    repo.save(parent);

    Manifestation manifestation =
        Manifestation.builder()
            .label(Locale.GERMAN, "Manifestation")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.GERMAN, "Manifestation"))
                    .build())
            .parent(
                RelationSpecification.<Manifestation>builder()
                    .subject(parent)
                    .title("Manifestation")
                    .build())
            .build();
    repo.save(manifestation);

    Manifestation actual = repo.getByUuid(manifestation.getUuid());
    assertThat(actual.getParents()).hasSize(1);
    assertThat(actual.getParents().get(0).getSubject().getUuid()).isEqualTo(parent.getUuid());

    repo.removeParent(manifestation, parent);

    actual = repo.getByUuid(manifestation.getUuid());
    assertThat(actual.getParents()).isEmpty();
  }

  // -------------------------------------------------------------------
  private Manifestation ensurePersistedParentManifestation()
      throws RepositoryException, ValidationException {
    var noteText = new StructuredContent();
    noteText.addContentBlock(new Text("some notes"));
    var note = new LocalizedStructuredContent();
    note.put(Locale.ENGLISH, noteText);
    var manifestation =
        Manifestation.builder()
            .label(new LocalizedText(Locale.ENGLISH, "A parent manifestation"))
            .manifestationType("SERIAL")
            .title(
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(new LocalizedText(Locale.ENGLISH, "A parent manifestation"))
                    .build())
            .title(
                Title.builder()
                    .titleType(new TitleType("sub", "sub"))
                    .text(new LocalizedText(Locale.ENGLISH, "...and its subtitle"))
                    .build())
            .note(note)
            .build();
    repo.save(manifestation);
    return manifestation;
  }

  private Manifestation prepareManifestation(
      Subject subject, Manifestation parent, List<Title> titles)
      throws RepositoryException, ValidationException {
    CorporateBody publisherAgent =
        CorporateBody.builder()
            .name(new LocalizedText(Locale.ENGLISH, "Publisher"))
            .label(new LocalizedText(Locale.ENGLISH, "Publisher label"))
            .identifier(Identifier.builder().namespace("publisherAgent").id("publisher").build())
            .build();
    corporateBodyRepository.save(publisherAgent);
    Person productionAgent =
        Person.builder()
            .name(new LocalizedText(Locale.ENGLISH, "Producer"))
            .label(new LocalizedText(Locale.ENGLISH, "Producer label"))
            .build();
    personRepository.save(productionAgent);
    HumanSettlement publicationPlace1 =
        HumanSettlement.builder()
            .name(new LocalizedText(Locale.forLanguageTag("und-Latn"), "München"))
            .label(Locale.forLanguageTag("und-Latn"), "München")
            .identifier(Identifier.builder().namespace("publicationPlace").id("Munich").build())
            .build();
    humanSettlementRepository.save(publicationPlace1);
    assertThat(publicationPlace1.getRefId()).isGreaterThan(0);
    HumanSettlement publicationPlace2 =
        HumanSettlement.builder()
            .name(new LocalizedText(Locale.forLanguageTag("de-Latn"), "Berlin"))
            .label(Locale.forLanguageTag("de-Latn"), "Berlin")
            .build();
    humanSettlementRepository.save(publicationPlace2);
    assertThat(publicationPlace2.getRefId()).isGreaterThan(0);

    Manifestation manifestation =
        Manifestation.builder()
            .label(Locale.GERMAN, "ein Label")
            .identifier(Identifier.builder().namespace("manifestation-id").id("test").build())
            .composition("composition")
            .expressionType(ExpressionType.builder().mainType("BOOK").subType("PRINT").build())
            .language(Locale.GERMAN)
            .mediaType("BOOK")
            .titles(titles)
            .subject(subject)
            .parent(new RelationSpecification<Manifestation>("The child's title", null, parent))
            .publicationInfo(
                PublicationInfo.builder()
                    .publisher(
                        Publisher.builder()
                            .agent(publisherAgent)
                            .location(publicationPlace1)
                            .location(publicationPlace2)
                            .build())
                    .navDateRange(
                        new LocalDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)))
                    .build())
            .productionInfo(
                ProductionInfo.builder()
                    .publisher(Publisher.builder().agent(productionAgent).build())
                    .navDateRange(
                        new LocalDateRange(LocalDate.of(-1000, 10, 1), LocalDate.of(5, 6, 30)))
                    .build())
            .navDate("2020-01-01")
            .build();
    return manifestation;
  }

  private Subject ensurePersistedSubject() throws RepositoryException, ValidationException {
    Subject subject =
        Subject.builder()
            .label(new LocalizedText(Locale.ENGLISH, "My subject"))
            .identifier(Identifier.builder().namespace("test").id("12345").build())
            .subjectType("SUBJECT_TYPE")
            .build();
    subjectRepository.save(subject);
    return subject;
  }

  private List<Title> prepareTitles() {
    List<Title> titles =
        new ArrayList<>(
            List.of(
                Title.builder()
                    .text(new LocalizedText(Locale.GERMAN, "Ein deutscher Titel"))
                    .titleType(new TitleType("main", "main"))
                    .textLocaleOfOriginalScript(Locale.GERMAN)
                    .textLocaleOfOriginalScript(Locale.ENGLISH)
                    .build(),
                Title.builder()
                    .text(new LocalizedText(Locale.GERMAN, "Untertitel"))
                    .titleType(new TitleType("main", "sub"))
                    .build(),
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(
                        new LocalizedText(
                            Locale.forLanguageTag("und-Latn"),
                            "Illustrierter Sonntag : das Blatt des gesunden Menschenverstandes. 1929 ## 31.03.1929"))
                    .textLocalesOfOriginalScripts(Collections.emptySet())
                    .build(),
                Title.builder()
                    .titleType(new TitleType("main", "main"))
                    .text(
                        new LocalizedText(
                            Locale.forLanguageTag("de-Latn"),
                            "Allegorien von Schrift, Stimme und Musik in Thomas Manns \"Doktor Faustus\""))
                    .build()));
    return titles;
  }
}
