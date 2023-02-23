package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.DbIdentifierMapper;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
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
class EventRepositoryImplTest extends AbstractRepositoryImplTest {

  private EventRepositoryImpl eventRepository;

  @Autowired DbIdentifierMapper dbIdentifierMapper;

  @BeforeEach
  public void beforeEach() {
    eventRepository = new EventRepositoryImpl(jdbi, cudamiConfig, dbIdentifierMapper);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  void saveAndRetrieveByUuid() throws RepositoryException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event event = Event.builder().label(label).name(label).build();

    eventRepository.save(event);

    assertThat(event.getUuid()).isNotNull();
    assertThat(event.getCreated()).isNotNull();
    assertThat(event.getLastModified()).isNotNull();
    assertThat(event.getLabel()).isEqualTo(label);
    assertThat(event.getName()).isEqualTo(label);

    Event retrievedEvent = eventRepository.getByUuid(event.getUuid());

    assertThat(retrievedEvent).isEqualTo(event);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() throws RepositoryException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    eventRepository.save(savedEvent);

    boolean success = eventRepository.delete(List.of(savedEvent.getUuid()));
    assertThat(success).isTrue();

    Event nonexistingEvent = eventRepository.getByUuid(savedEvent.getUuid());
    assertThat(nonexistingEvent).isNull();

    boolean nonsuccess =
        eventRepository.delete(List.of(savedEvent.getUuid())); // second attempt must fail!
    assertThat(nonsuccess).isFalse();
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() throws RepositoryException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event event = Event.builder().label(label).name(label).build();
    eventRepository.save(event);

    final LocalizedText newName = new LocalizedText(Locale.GERMAN, "aktualisierter Test");
    event.setName(newName);
    eventRepository.update(event);

    Event actual = eventRepository.getByUuid(event.getUuid());

    assertThat(actual).isEqualTo(event);
  }

  @DisplayName("can retrieve all events with paging")
  @Test
  void findAllPaged() throws RepositoryException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    eventRepository.save(savedEvent);

    PageResponse<Event> pageResponse =
        eventRepository.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedEvent);
  }

  @DisplayName("can retrieve all events with sorting")
  @Test
  void findAllPagedAndSorted() throws RepositoryException {
    final LocalizedText label1 = new LocalizedText(Locale.GERMAN, "Test 1");
    Event savedEvent1 = Event.builder().label(label1).name(label1).build();
    eventRepository.save(savedEvent1);

    final LocalizedText label2 = new LocalizedText(Locale.GERMAN, "Test 2");
    Event savedEvent2 = Event.builder().label(label2).name(label2).build();
    eventRepository.save(savedEvent2);

    PageResponse<Event> pageResponse =
        eventRepository.find(
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
  void findFiltered() throws RepositoryException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    eventRepository.save(savedEvent);

    PageResponse<Event> pageResponse =
        eventRepository.find(
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
  void noMatches() throws RepositoryException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Event savedEvent = Event.builder().label(label).name(label).build();
    eventRepository.save(savedEvent);

    PageResponse<Event> pageResponse =
        eventRepository.find(
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
