package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@DisplayName("The Identifiable Repository")
class IdentifiableRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<IdentifiableRepositoryImpl> {

  @Autowired private SubjectRepository subjectRepository;
  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;

  @BeforeEach
  public void beforeEach() {
    repo =
        new IdentifiableRepositoryImpl(
            jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
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
  @DisplayName("retrieve one identifiable")
  void testGetByUuid() throws RepositoryException {
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(UUID.randomUUID());
    identifiable.setCreated(LocalDateTime.now());
    identifiable.setType(IdentifiableType.RESOURCE);
    identifiable.setLabel("test");
    identifiable.setLastModified(LocalDateTime.now());

    repo.save(identifiable);

    Identifiable actual = (Identifiable) repo.getByUuid(identifiable.getUuid());
    assertThat(actual.equals(identifiable));
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
        "(test.split_label::TEXT[] @> :searchTermArray::TEXT[] OR jsonb_path_exists(test.description, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath))";
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
    Subject subject =
        Subject.builder()
            .label(new LocalizedText(Locale.ENGLISH, "My first subject"))
            .identifier(Identifier.builder().namespace("test").id("12345").build())
            .type("SUBJECT_TYPE")
            .build();
    subjectRepository.save(subject);

    Identifiable identifiable =
        Identifiable.builder()
            .type(IdentifiableType.ENTITY)
            .identifiableObjectType(IdentifiableObjectType.IDENTIFIABLE)
            .label(Locale.GERMAN, "Test")
            .subject(subject)
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

  @DisplayName("can update and return an Identifiable with updated lastModified timestamp")
  @Test
  public void testUpdate() throws RepositoryException {
    Identifiable identifiable =
        Identifiable.builder()
            .type(IdentifiableType.ENTITY)
            .identifiableObjectType(IdentifiableObjectType.IDENTIFIABLE)
            .label(Locale.GERMAN, "Test")
            .build();
    repo.save(identifiable);

    Identifiable beforeUpdate = createDeepCopy(identifiable);

    LocalDateTime timestampBeforeUpdate = LocalDateTime.now();
    repo.update(identifiable);
    LocalDateTime timestampAfterUpdate = LocalDateTime.now();

    // The last modified timestamp must be modified and must be between the time before and
    // after the uodate
    assertThat(identifiable.getLastModified()).isAfter(timestampBeforeUpdate);
    assertThat(identifiable.getLastModified()).isBefore(timestampAfterUpdate);

    // Now, we verify, if the rest if the same. To enable the assertion, we just set
    // the last modified timestamp to the value before the update and check for
    // equality of the updated and the to-be-updated object
    beforeUpdate.setLastModified(identifiable.getLastModified());
    assertThat(identifiable).isEqualToComparingFieldByField(beforeUpdate);

    // Finally, we ensure that the Identifiable, with which the update method
    // works is identical to the Identificable, which is persisted in the database
    Identifiable persisted = (Identifiable) repo.getByUuid(identifiable.getUuid());
    assertThat(identifiable).isEqualToComparingFieldByField(persisted);
  }

  @Test
  @DisplayName("returns properly sized pages on search")
  void testSearchPageSize() throws RepositoryException {
    // Insert a bunch of DigitalObjects with labels
    IntStream.range(0, 20)
        .forEach(
            i -> {
              try {
                repo.save(createDigitalObjectWithLabels("test" + i));
              } catch (RepositoryException e) {
                throw new RuntimeException(e);
              }
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
    String in =
        "A funny text with comma, a hyphen-separated word (unusual in English though) and some other stuff...";
    final String[] expected =
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
    String[] out = repo.splitToArray(in);
    assertThat(out).containsExactly(expected);

    in = "\"Here we have quotes and a word-with-two hyphens!\"";
    final String[] expected1 =
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
    out = repo.splitToArray(in);
    assertThat(out).containsExactly(expected1);

    in = "something easy";
    final String[] expected2 = new String[] {"something", "easy"};
    out = repo.splitToArray(in);
    assertThat(out).containsExactly(expected2);

    in = "one";
    final String[] expected3 = new String[] {"one"};
    out = repo.splitToArray(in);
    assertThat(out).containsExactly(expected3);
  }

  @Test
  @DisplayName("can split umlauts")
  void testSplitterWithUmlauts() {
    String[] expected = {"münchen", "bayerische", "staatsbibliothek"};
    String[] actual = repo.splitToArray("München, Bayerische Staatsbibliothek");
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("can split text with numbers, too")
  void testSplitterWithNumbers() {
    String[] expected = {"80333", "münchen", "ludwigstr", "16"};
    String[] actual = repo.splitToArray("80333 München, Ludwigstr. 16");
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("can split in foreign scripts")
  void testSplitterWithForeignScripts() {
    String[] expected = {"古學二千文", "名山勝槩圖", "本草求真", "8"};
    String[] actual = repo.splitToArray("古學二千文 名山勝槩圖, 本草求真. 8");
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("split a label into an array")
  void testSplitLocalizedText() {
    String[] expected = {
      "bayerische", "staatsbibliothek", "münchen", "bavarian", "state", "library", "munich"
    };
    LocalizedText label = new LocalizedText(Locale.GERMAN, "Bayerische Staatsbibliothek, München");
    label.put(Locale.ENGLISH, "Bavarian State Library, Munich");
    String[] actual = repo.splitToArray(label);
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("save and update `split_label`")
  void testSaveUpdateOfSplitLabel() throws RepositoryException {
    // test save method
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setLabel(
        new LocalizedText(Locale.ENGLISH, "1 not so short Label to check the Label-Splitting"));

    repo.save(digitalObject);
    assertThat(digitalObject.getUuid()).isNotNull();

    String[] splitLabelDb =
        jdbi.withHandle(
            h ->
                h.select(
                        "select split_label from identifiables where uuid = ?;",
                        digitalObject.getUuid())
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
    LocalizedText label = new LocalizedText();
    label.setText(Locale.ENGLISH, "An English label, no. 1");
    label.setText(Locale.GERMAN, "Ein deutsches Label, nr. 2");
    digitalObject.setLabel(label);
    repo.update(digitalObject);

    String[] splitLabelUpdated =
        jdbi.withHandle(
            h ->
                h.select(
                        "select split_label from identifiables where uuid = ?;",
                        digitalObject.getUuid())
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
