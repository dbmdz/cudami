package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

  @DisplayName("deletes UrlAliases, too")
  @Test
  public void deleteIncludesUrlAliases() throws Exception {
    Identifiable identifiable1 = createIdentifiable();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setPrimary(true);
    urlAlias.setTargetLanguage(Locale.GERMAN);
    urlAlias.setSlug("label1");
    urlAlias.setTarget(identifiable1);
    localizedUrlAliases.add(urlAlias);
    identifiable1.setLocalizedUrlAliases(localizedUrlAliases);

    Identifiable identifiable2 = createIdentifiable();
    LocalizedUrlAliases localizedUrlAliases2 = new LocalizedUrlAliases();
    UrlAlias urlAlias2 = new UrlAlias();
    urlAlias2.setPrimary(true);
    urlAlias2.setTargetLanguage(Locale.GERMAN);
    urlAlias2.setSlug("label2");
    urlAlias2.setTarget(identifiable2);
    localizedUrlAliases2.add(urlAlias2);
    identifiable2.setLocalizedUrlAliases(localizedUrlAliases2);

    Set<Identifiable> identifiables = Set.of(identifiable1, identifiable2);
    repo.save(identifiable1);
    repo.save(identifiable2);

    repo.delete(identifiables);

    assertThat(urlAliasRepository.getByIdentifiable(identifiable1)).isNullOrEmpty();
    assertThat(urlAliasRepository.getByIdentifiable(identifiable2)).isNullOrEmpty();
  }

  @Test
  @DisplayName("can retrieve a single identifiable by its uuid")
  void testGetByUuid() throws RepositoryException, ValidationException {
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
  @DisplayName("can retrieve a multiple identifiables by their uuids")
  void testGetByUuids() {
    Map<UUID, Identifiable> persistedIdentifiables =
        IntStream.range(0, 10)
            .mapToObj(
                i -> {
                  Identifiable identifiable = new Identifiable();
                  identifiable.setUuid(UUID.randomUUID());
                  identifiable.setCreated(LocalDateTime.now());
                  identifiable.setType(IdentifiableType.RESOURCE);
                  identifiable.setLabel("test");
                  identifiable.setLastModified(LocalDateTime.now());

                  try {
                    repo.save(identifiable);
                  } catch (RepositoryException | ValidationException e) {
                    throw new RuntimeException(e);
                  }

                  return identifiable;
                })
            .collect(Collectors.toMap(Identifiable::getUuid, i -> i));

    Map<UUID, Identifiable> actualIdentifiables =
        persistedIdentifiables.keySet().stream()
            .map(
                u -> {
                  try {
                    return (Identifiable) repo.getByUuid(u);
                  } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toMap(Identifiable::getUuid, i -> i));

    assertThat(actualIdentifiables).isEqualTo(persistedIdentifiables);
  }

  @Test
  @DisplayName("can return a subset of identifiables, queried by their uuid")
  void getByUuidWithPartialResult() throws RepositoryException, ValidationException {
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(UUID.randomUUID());
    identifiable.setCreated(LocalDateTime.now());
    identifiable.setType(IdentifiableType.RESOURCE);
    identifiable.setLabel("test");
    identifiable.setLastModified(LocalDateTime.now());
    repo.save(identifiable);

    List<Identifiable> actual =
        repo.getByUuids(
            List.of(
                UUID.randomUUID(),
                identifiable.getUuid(),
                UUID.randomUUID(),
                identifiable.getUuid()));

    // We don't get duplicates here!
    assertThat(actual).containsExactlyInAnyOrder(identifiable);
  }

  @Test
  @DisplayName(
      "can return an empty list of identifiables, when non of them was found by querying by uuid")
  void getEmptyListOfIdentifiablesByGetByUuids() throws RepositoryException {
    List<Identifiable> actual = repo.getByUuids(List.of(UUID.randomUUID(), UUID.randomUUID()));
    assertThat(actual).isEmpty();
  }

  @Test
  @DisplayName("returns null when a single getByUUID finds no matches")
  void getNullIdentifiablesByUuid() throws RepositoryException {
    Identifiable actual = (Identifiable) repo.getByUuid(UUID.randomUUID());
    assertThat(actual).isNull();
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
  void testSave() throws RepositoryException, ValidationException {
    Subject subject =
        Subject.builder()
            .label(new LocalizedText(Locale.ENGLISH, "My first subject"))
            .identifier(Identifier.builder().namespace("test").id("12345").build())
            .subjectType("SUBJECT_TYPE")
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
  public void testUpdate() throws RepositoryException, ValidationException {
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

    // The last modified timestamp must be modified and must be between the time
    // before and
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
  void testSearchPageSize() throws RepositoryException, ValidationException {
    // Insert a bunch of DigitalObjects with labels
    IntStream.range(0, 20)
        .forEach(
            i -> {
              try {
                repo.save(createDigitalObjectWithLabels("test" + i));
              } catch (RepositoryException | ValidationException e) {
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
  void testSaveUpdateOfSplitLabel() throws RepositoryException, ValidationException {
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

  @Test
  @DisplayName("test where clause creation with OR")
  void testGetWhereClauses() {
    Filtering f =
        Filtering.builder()
            .filterCriterion(
                FilterLogicalOperator.OR,
                new FilterCriterion<String>("label.de-Latn", FilterOperation.CONTAINS, "some text"))
            .filterCriterion(
                FilterLogicalOperator.OR,
                new FilterCriterion<String>(
                    "description.de-Latn", FilterOperation.CONTAINS, "some text"))
            .filterCriterion(
                FilterLogicalOperator.AND,
                new FilterCriterion<LocalDate>(
                    "lastModified", FilterOperation.GREATER_THAN, LocalDate.of(2020, 1, 1)))
            .build();
    StringBuilder actual = new StringBuilder();
    Map<String, Object> mappings = new HashMap<>();
    repo.addFiltering(f, actual, mappings);
    String expected =
        " "
            + """
        WHERE (i.split_label::TEXT[] @> :searchTermArray_1::TEXT[]
        OR jsonb_path_exists(i.description, ('$.\"de-Latn\" ? (@ like_regex \"' || :searchTerm_2 || '\" flag \"iq\")')::jsonpath))
        AND ((i.last_modified > :filtervalue_3))"""
                .replace("\n", " ");
    assertThat(actual.toString()).isEqualTo(expected);
    assertThat(mappings).size().isEqualTo(3);
    assertThat(mappings)
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "searchTermArray_1",
                new String[] {"some", "text"},
                "searchTerm_2",
                "some text",
                "filtervalue_3",
                LocalDate.of(2020, 1, 1)));
  }
}
