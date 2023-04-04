package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EventService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Event controller")
public class EventController extends AbstractIdentifiableController<Event> {

  private final LocaleService localeService;
  private final EventService service;

  public EventController(LocaleService localeService, EventService eventService) {
    this.localeService = localeService;
    this.service = eventService;
  }

  @Operation(summary = "Get count of events")
  @GetMapping(
      value = {
        "/v6/events/count",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return service.count();
  }

  @Operation(summary = "Delete an event")
  @DeleteMapping(
      value = {"/v6/events/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the event") @PathVariable("uuid") UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = service.delete(Event.builder().uuid(uuid).build());
    } catch (ServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "get all events as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/events"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Event> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Event.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.find(pageRequest);
  }

  @Operation(
      summary = "Get an event by identifier",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/events/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<Event> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get an event by uuid")
  @GetMapping(
      value = {"/v6/events/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Event> getByUuid(
      @Parameter(
              example = "",
              description = "UUID of the event, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws ServiceException {
    Event event = service.getByExample(Event.builder().uuid(uuid).build());
    return new ResponseEntity<>(event, event != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary = "Get languages of all events",
      description = "Get languages of all events",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/events/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return service.getLanguages();
  }

  @Override
  protected IdentifiableService<Event> getService() {
    return service;
  }

  @Operation(summary = "Save an event")
  @PostMapping(
      value = {"/v6/events"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Event save(@RequestBody Event event, BindingResult errors)
      throws ServiceException, ValidationException {
    service.save(event);
    return event;
  }

  @Operation(summary = "Update an event")
  @PutMapping(
      value = {"/v6/events/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Event update(@PathVariable UUID uuid, @RequestBody Event event, BindingResult errors)
      throws ServiceException, ValidationException {
    assert Objects.equals(uuid, event.getUuid());
    service.update(event);
    return event;
  }
}
