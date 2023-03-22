package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EventService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(EventController.class)
@DisplayName("The EventController")
class EventControllerTest extends BaseControllerTest {

  @MockBean private EventService eventService;
  @MockBean private LocaleService localeService;

  @DisplayName("can return the amount of events")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/events/count"})
  public void testCount(String path) throws Exception {
    when(eventService.count()).thenReturn(42L);
    testGetJsonString(path, "42");
  }

  @DisplayName("can retrieve an event by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/events/599a120c-2dd5-11e8-b467-0ed5f89f718b"})
  public void getByUuid(String path) throws Exception {
    final LocalizedText name = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier =
        Identifier.builder().namespace("event-namespace").id("event-id").build();
    Event event =
        Event.builder()
            .uuid(extractFirstUuidFromPath(path))
            .name(name)
            .label(name)
            .identifier(identifier)
            .build();
    when(eventService.getByUuid(eq(UUID.fromString("599a120c-2dd5-11e8-b467-0ed5f89f718b"))))
        .thenReturn(event);
    testJson(path);
  }

  @DisplayName("can retrieve an event by its identifier")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/events/identifier/event-namespace:event-id"})
  public void getByIdentifier(String path) throws Exception {
    final UUID uuid = UUID.fromString("599a120c-2dd5-11e8-b467-0ed5f89f718b");
    final LocalizedText name = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier =
        Identifier.builder().namespace("event-namespace").id("event-id").build();
    Event event = Event.builder().uuid(uuid).name(name).label(name).identifier(identifier).build();
    when(eventService.getByIdentifier(eq(identifier.getNamespace()), eq(identifier.getId())))
        .thenReturn(event);
    testJson(path);
  }

  @DisplayName("can delete an event")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/events/09baa24e-0918-4b96-8ab1-f496b02af73a"})
  void deleteEvent(String path) throws Exception {
    UUID uuid = UUID.fromString("09baa24e-0918-4b96-8ab1-f496b02af73a");
    when(eventService.delete(eq(uuid))).thenReturn(true);

    testDeleteSuccessful(path);

    verify(eventService, times(1)).delete(eq(uuid));
  }

  @DisplayName("can not delete an already deleted event")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/events/09baa24e-0918-4b96-8ab1-f496b02af73a"})
  void deleteEventTwice(String path) throws Exception {
    UUID uuid = UUID.fromString("09baa24e-0918-4b96-8ab1-f496b02af73a");
    when(eventService.delete(eq(uuid))).thenReturn(false);

    testDeleteNotFound(path);
  }

  @DisplayName("can save an event")
  @Test
  void save() throws Exception {
    String jsonBody =
        """
        {
          "identifiableObjectType": "EVENT",
          "identifiers": [
            {
              "objectType": "IDENTIFIER",
              "id": "event-id",
              "namespace": "event-namespace"
            }
          ],
          "label": {
            "de": "Test"
          },
          "type": "ENTITY",
          "refId": 0,
          "name": {
            "de": "Test"
          }
        }
        """;

    final LocalizedText name = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier =
        Identifier.builder().namespace("event-namespace").id("event-id").build();
    Event expectedEventToBeSaved =
        Event.builder().name(name).label(name).identifier(identifier).build();

    testPostJsonWithState("/v6/events", jsonBody, 200);

    verify(eventService, times(1)).save(eq(expectedEventToBeSaved));
  }

  @DisplayName("can update an event")
  @Test
  void update() throws Exception {
    String jsonBody =
        """
        {
          "identifiableObjectType": "EVENT",
          "identifiers": [
            {
              "objectType": "IDENTIFIER",
              "id": "event-id",
              "namespace": "event-namespace"
            }
          ],
          "label": {
            "de": "Test"
          },
          "type": "ENTITY",
          "refId": 0,
          "name": {
            "de": "Test"
          },
          "uuid": "09baa24e-0918-4b96-8ab1-f496b02af73a"
        }
        """;

    final LocalizedText name = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier =
        Identifier.builder().namespace("event-namespace").id("event-id").build();
    Event expectedEventToBeSaved =
        Event.builder()
            .uuid("09baa24e-0918-4b96-8ab1-f496b02af73a")
            .name(name)
            .label(name)
            .identifier(identifier)
            .build();

    testPutJsonWithState("/v6/events/09baa24e-0918-4b96-8ab1-f496b02af73a", jsonBody, 200);

    verify(eventService, times(1)).update(eq(expectedEventToBeSaved));
  }

  @DisplayName("can return the languages of all events")
  @Test
  public void allLanguages() throws Exception {
    when(eventService.getLanguages()).thenReturn(List.of(Locale.GERMAN, Locale.ENGLISH));
    testGetJsonString("/v6/events/languages", "[\"de\",\"en\"]");
  }

  @DisplayName("shall find a filtered list of events")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/events/?pageNumber=0&pageSize=10&filter=label:like:foo"})
  public void relatedByPredicate(String path) throws Exception {
    List<Event> events =
        List.of(
            Event.builder()
                .created("2020-10-20T14:38:07.757894")
                .identifier("gnd", "1234567-8", "30b59f1e-aa2f-4ae5-b9a4-fa336e21ad8e")
                .label(Locale.GERMAN, "foo")
                .name(new LocalizedText(Locale.GERMAN, "foo"))
                .lastModified("2021-02-25T09:05:34.039316")
                .uuid("2d72fc41-e7a7-4666-a76f-38e3d565eb48")
                .build());

    PageResponse<Event> pageResponse = PageResponse.builder().withContent(events).build();

    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber(0)
            .pageSize(10)
            .filtering(
                Filtering.builder()
                    .add(FilterCriterion.builder().withExpression("label").contains("foo").build())
                    .build())
            .build();
    when(eventService.find(eq(pageRequest))).thenReturn(pageResponse);

    testJson(path, "/v6/events/listresult.json");
  }
}
