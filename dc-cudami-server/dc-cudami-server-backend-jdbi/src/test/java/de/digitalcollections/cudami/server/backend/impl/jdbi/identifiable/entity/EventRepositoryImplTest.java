package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {EventRepositoryImpl.class})
@DisplayName("The Event Repository")
class EventRepositoryImplTest extends AbstractIdentifiableRepositoryImplTest<EventRepositoryImpl> {

  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;

  @BeforeEach
  public void beforeEach() {
    repo = new EventRepositoryImpl(jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  void saveAndRetrieveByUuid() throws RepositoryException, ValidationException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event event = Event.builder().label(label).name(label).build();

    repo.save(event);

    assertThat(event.getUuid()).isNotNull();
    assertThat(event.getCreated()).isNotNull();
    assertThat(event.getLastModified()).isNotNull();
    assertThat(event.getLabel()).isEqualTo(label);
    assertThat(event.getName()).isEqualTo(label);

    Event retrievedEvent = repo.getByUuid(event.getUuid());

    assertThat(retrievedEvent).isEqualTo(event);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() throws RepositoryException, ValidationException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    repo.save(savedEvent);

    int affected = repo.deleteByUuids(List.of(savedEvent.getUuid()));
    assertEquals(1, affected);

    Event nonexistingEvent = repo.getByUuid(savedEvent.getUuid());
    assertThat(nonexistingEvent).isNull();

    affected = repo.deleteByUuids(List.of(savedEvent.getUuid())); // second attempt must fail!
    assertEquals(0, affected);
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() throws RepositoryException, ValidationException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event event = Event.builder().label(label).name(label).build();
    repo.save(event);

    final LocalizedText newName = new LocalizedText(Locale.GERMAN, "aktualisierter Test");
    event.setName(newName);
    repo.update(event);

    Event actual = repo.getByUuid(event.getUuid());

    assertThat(actual).isEqualTo(event);
  }

  @DisplayName("can retrieve all events with paging")
  @Test
  void findAllPaged() throws RepositoryException, ValidationException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    repo.save(savedEvent);

    PageResponse<Event> pageResponse =
        repo.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedEvent);
  }

  @DisplayName("can retrieve all events with sorting")
  @Test
  void findAllPagedAndSorted() throws RepositoryException, ValidationException {
    final LocalizedText label1 = new LocalizedText(Locale.GERMAN, "Test 1");
    Event savedEvent1 = Event.builder().label(label1).name(label1).build();
    repo.save(savedEvent1);

    final LocalizedText label2 = new LocalizedText(Locale.GERMAN, "Test 2");
    Event savedEvent2 = Event.builder().label(label2).name(label2).build();
    repo.save(savedEvent2);

    PageResponse<Event> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .sorting(
                    Sorting.builder()
                        .order(Order.builder().property("name").direction(Direction.ASC).build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedEvent1, savedEvent2);
  }

  @DisplayName("can retrieve events with filtering")
  @Test
  void findFiltered() throws RepositoryException, ValidationException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    repo.save(savedEvent);

    PageResponse<Event> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("name")
                                .isEquals("Test")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedEvent);
  }

  @DisplayName("can return an empty filtered set when no matches are found")
  @Test
  void noMatches() throws RepositoryException, ValidationException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    repo.save(savedEvent);

    PageResponse<Event> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("name")
                                .isEquals("kein Test")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).isEmpty();
  }
}
