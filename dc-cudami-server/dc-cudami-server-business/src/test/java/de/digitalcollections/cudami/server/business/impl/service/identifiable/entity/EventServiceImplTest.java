package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EventRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The EventService")
class EventServiceImplTest extends AbstractServiceImplTest {

  private EventServiceImpl eventService;
  private IdentifierService identifierService;
  private HookProperties hookProperties;
  private LocaleService localeService;
  private UrlAliasService urlAliasService;

  EventRepository eventRepository;

  @Override
  @BeforeEach
  public void beforeEach() throws Exception {
    super.beforeEach();
    eventRepository = mock(EventRepository.class);
    identifierService = mock(IdentifierService.class);
    hookProperties = mock(HookProperties.class);
    localeService = mock(LocaleService.class);
    urlAliasService = mock(UrlAliasService.class);
    eventService =
        new EventServiceImpl(
            eventRepository,
            identifierService,
            urlAliasService,
            hookProperties,
            localeService,
            cudamiConfig);
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  public void saveAndRetrieveByUuid()
      throws RepositoryException, ServiceException, ValidationException {
    final UUID uuid = UUID.randomUUID();
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier =
        Identifier.builder().namespace("event-namespace").id("event-id").build();
    Event savedEvent = Event.builder().label(label).name(label).identifier(identifier).build();

    // Let the mocked repository just return the event after save and fill the uuid
    doAnswer(
            invocation -> {
              Object[] args = invocation.getArguments();
              ((Event) args[0]).setUuid(uuid);
              return null;
            })
        .when(eventRepository)
        .save(eq(savedEvent));

    when(eventRepository.getByExamples(eq(List.of(savedEvent)))).thenReturn(List.of(savedEvent));

    eventService.save(savedEvent);

    Event actual = eventService.getByExample(savedEvent);

    assertThat(actual).isEqualTo(savedEvent);
  }

  @DisplayName("can save and retrieve by identifier")
  @Test
  public void saveAndRetrieveByIdentifier()
      throws RepositoryException, ServiceException, ValidationException {
    final UUID uuid = UUID.randomUUID();
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier =
        Identifier.builder().namespace("event-namespace").id("event-id").build();
    Event savedEvent = Event.builder().label(label).name(label).identifier(identifier).build();

    // Let the mocked repository just return the event after save and fill the uuid
    doAnswer(
            invocation -> {
              Object[] args = invocation.getArguments();
              ((Event) args[0]).setUuid(uuid);
              return null;
            })
        .when(eventRepository)
        .save(eq(savedEvent));

    when(eventRepository.getByExample(eq(savedEvent))).thenReturn(savedEvent);
    when(eventRepository.getByIdentifier(eq(identifier))).thenReturn(savedEvent);
    when(identifierService.saveForIdentifiable(eq(savedEvent), any()))
        .thenReturn(Set.of(identifier));

    eventService.save(savedEvent);

    Event actual = eventService.getByIdentifier(identifier);

    assertThat(actual).isEqualTo(savedEvent);
  }
}
