package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Subject;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The Identifiable Repository")
@Sql(scripts = "classpath:cleanup_database.sql")
class IdentifiableRepositoryImplTest {

  IdentifiableRepositoryImpl repo;

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired Jdbi jdbi;
  @Autowired CudamiConfig cudamiConfig;

  @Autowired SubjectRepository subjectRepository;

  @BeforeEach
  public void beforeEach() {
    repo = new IdentifiableRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  private DigitalObject createDigitalObjectWithLabels(String label) {
    DigitalObject digitalObject = new DigitalObject();
    LocalizedText labelText = new LocalizedText();
    labelText.setText(Locale.GERMAN, label);
    labelText.setText(Locale.ENGLISH, label);
    digitalObject.setLabel(labelText);
    LocalizedStructuredContent description = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    Paragraph paragraph = new Paragraph(label);
    structuredContent.addContentBlock(paragraph);
    description.put(Locale.GERMAN, structuredContent);
    description.put(Locale.ENGLISH, structuredContent);
    digitalObject.setDescription(description);
    return digitalObject;
  }

  @Test
  @DisplayName("retrieve one digital object")
  void testGetByUuid() {
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(UUID.randomUUID());
    identifiable.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    identifiable.setType(IdentifiableType.RESOURCE);
    identifiable.setLabel("test");
    identifiable.setLastModified(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));

    identifiable = this.repo.save(identifiable);

    Identifiable actual = this.repo.getByUuid(identifiable.getUuid());
    assertThat(actual).isEqualTo(identifiable);
  }

  @Test
  @DisplayName("returns expected sql string")
  void testGetCommonSearchSql() {
    String actual = repo.getCommonSearchSql("test", "\"phrase term\"");
    String expected =
        "(jsonb_path_exists(test.label, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath) OR jsonb_path_exists(test.description, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath))";
    assertThat(actual).isEqualTo(expected);

    actual = repo.getCommonSearchSql("test", "search term");
    expected =
        "(test.split_label @> :searchTermArray::TEXT[] OR jsonb_path_exists(test.description, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath))";
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @DisplayName("test getOrderBy")
  void testGetOrderBy() {
    Sorting sorting = new Sorting(new Order(Direction.DESC, "lastModified"), new Order("uuid"));
    assertThat(repo.getOrderBy(sorting)).isEqualTo("i.last_modified DESC, i.uuid ASC");

    sorting = new Sorting(Order.builder().property("label").subProperty("de").build());
    assertThat(repo.getOrderBy(sorting))
        .isEqualTo("lower(COALESCE(i.label->>'de', i.label->>'')) COLLATE \"ucs_basic\" ASC");

    sorting.getOrders().add(new Order(Direction.DESC, "lastModified"));
    assertThat(repo.getOrderBy(sorting))
        .isEqualTo(
            "lower(COALESCE(i.label->>'de', i.label->>'')) COLLATE \"ucs_basic\" ASC, i.last_modified DESC");

    sorting =
        new Sorting(
            new Order(Direction.DESC, "created"),
            Order.builder().property("label").subProperty("de").build(),
            Order.builder()
                .property("label")
                .subProperty("en")
                .direction(Direction.DESC)
                .ignoreCase(false)
                .build());
    assertThat(repo.getOrderBy(sorting))
        .isEqualTo(
            "i.created DESC, "
                + "lower(COALESCE(i.label->>'de', i.label->>'')) COLLATE \"ucs_basic\" ASC, "
                + "COALESCE(i.label->>'en', i.label->>'') COLLATE \"ucs_basic\" DESC");
  }

  @Test
  @DisplayName("saves an Identifiable and fills uuid and timestamps")
  void testSave() throws RepositoryException {
    Subject subject1 =
        Subject.builder()
            .label(new LocalizedText(Locale.ENGLISH, "My first subject"))
            .identifier(Identifier.builder().namespace("test").id("12345").build())
            .type("SUBJECT_TYPE")
            .build();
    subjectRepository.save(subject1);
    Subject subject2 =
        Subject.builder()
            .label(new LocalizedText(Locale.ENGLISH, "My second subject"))
            .identifier(Identifier.builder().namespace("test").id("123456").build())
            .type("SUBJECT_TYPE")
            .build();
    subjectRepository.save(subject2);

    Identifiable identifiable =
        Identifiable.builder()
            .type(IdentifiableType.ENTITY)
            .identifiableObjectType(IdentifiableObjectType.IDENTIFIABLE)
            .label(Locale.GERMAN, "Test")
            .subject(subject1)
            .subject(subject2)
            .build();

    assertThat(identifiable.getCreated()).isNull();
    assertThat(identifiable.getLastModified()).isNull();
    assertThat(identifiable.getUuid()).isNull();

    repo.save(identifiable);

    assertThat(identifiable.getCreated()).isNotNull();
    assertThat(identifiable.getLastModified()).isNotNull();
    assertThat(identifiable.getUuid()).isNotNull();

    Identifiable persisted = (Identifiable) repo.getByUuid(identifiable.getUuid());
    assertThat(identifiable).isEqualToComparingFieldByField(persisted);
  }

    DigitalObject actual = (DigitalObject) repo.save(digitalObject);

    assertThat(actual).isEqualTo(digitalObject);
    assertThat(actual.getCreated()).isNotNull();
    assertThat(actual.getLastModified()).isNotNull();
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("returns properly sized pages on search")
  void testSearchPageSize() {
    // Insert a bunch of DigitalObjects with labels
    IntStream.range(0, 20)
        .forEach(
            i -> {
              repo.save(createDigitalObjectWithLabels("test" + i));
            });

    String searchTerm = "test";
    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageSize(10);
    pageRequest.setPageNumber(0);
    pageRequest.setSearchTerm(searchTerm);

    PageResponse response = repo.find(pageRequest);

    List<Identifiable> content = response.getContent();
    assertThat(content).hasSize(10);
  }

  @Test
  @DisplayName("test string splitting method")
  void testSplitter() {
    var in =
        "A funny text with comma, a hyphen-separated word (unusual in English though) and some other stuff...";
    final var expected =
        new String[] {
          "hyphen",
          "separated",
          "a",
          "funny",
          "text",
          "with",
          "comma",
          "a",
          "hyphen-separated",
          "word",
          "unusual",
          "in",
          "english",
          "though",
          "and",
          "some",
          "other",
          "stuff"
        };
    String[] out = IdentifiableRepository.splitToArray(in);
    assertThat(out).containsExactly(expected);

    in = "\"Here we have quotes and a word-with-two hyphens!\"";
    final var expected1 =
        new String[] {
          "word",
          "with",
          "two",
          "here",
          "we",
          "have",
          "quotes",
          "and",
          "a",
          "word-with-two",
          "hyphens"
        };
    out = IdentifiableRepository.splitToArray(in);
    assertThat(out).containsExactly(expected1);

    in = "something easy";
    final var expected2 = new String[] {"something", "easy"};
    out = IdentifiableRepository.splitToArray(in);
    assertThat(out).containsExactly(expected2);

    in = "one";
    final var expected3 = new String[] {"one"};
    out = IdentifiableRepository.splitToArray(in);
    assertThat(out).containsExactly(expected3);
  }

  @Test
  @DisplayName("can split umlauts")
  void testSplitterWithUmlauts() {
    String[] expected = {"münchen", "bayerische", "staatsbibliothek"};
    String[] actual = IdentifiableRepository.splitToArray("München, Bayerische Staatsbibliothek");
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("can split text with numbers, too")
  void testSplitterWithNumbers() {
    String[] expected = {"80333", "münchen", "ludwigstr", "16"};
    String[] actual = IdentifiableRepository.splitToArray("80333 München, Ludwigstr. 16");
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("can split in foreign scripts")
  void testSplitterWithForeignScripts() {
    String[] expected = {"古學二千文", "名山勝槩圖", "本草求真", "8"};
    String[] actual = IdentifiableRepository.splitToArray("古學二千文 名山勝槩圖, 本草求真. 8");
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("split a label into an array")
  void testSplitLocalizedText() {
    String[] expected = {
      "bayerische", "staatsbibliothek", "münchen", "bavarian", "state", "library", "munich"
    };
    var label = new LocalizedText(Locale.GERMAN, "Bayerische Staatsbibliothek, München");
    label.put(Locale.ENGLISH, "Bavarian State Library, Munich");
    var actual = repo.splitToArray(label);
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("save and update `split_label`")
  void testSaveUpdateOfSplitLabel() {
    // test save method
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setLabel(
        new LocalizedText(Locale.ENGLISH, "1 not so short Label to check the Label-Splitting"));

    DigitalObject savedDigitalObject = (DigitalObject) repo.save(digitalObject);
    assertThat(savedDigitalObject.getUuid()).isNotNull();

    String[] splitLabelDb =
        jdbi.withHandle(
            h ->
                h.select(
                        "select split_label from identifiables where uuid = ?;",
                        savedDigitalObject.getUuid())
                    .mapTo(String[].class)
                    .findOne()
                    .orElse(null));
    assertThat(splitLabelDb).isNotNull().isNotEmpty();
    assertThat(splitLabelDb)
        .containsExactly(
            new String[] {
              "label",
              "splitting",
              "1",
              "not",
              "so",
              "short",
              "label",
              "to",
              "check",
              "the",
              "label-splitting"
            });

    // test update method
    var label = new LocalizedText();
    label.setText(Locale.ENGLISH, "An English label, no. 1");
    label.setText(Locale.GERMAN, "Ein deutsches Label, nr. 2");
    savedDigitalObject.setLabel(label);
    repo.update(savedDigitalObject);

    String[] splitLabelUpdated =
        jdbi.withHandle(
            h ->
                h.select(
                        "select split_label from identifiables where uuid = ?;",
                        savedDigitalObject.getUuid())
                    .mapTo(String[].class)
                    .findOne()
                    .orElse(null));
    assertThat(splitLabelUpdated).isNotNull().isNotEmpty();
    assertThat(splitLabelUpdated)
        .containsExactlyInAnyOrder(
            new String[] {
              "an", "english", "label", "no", "1", "ein", "deutsches", "label", "nr", "2"
            });
  }
}
