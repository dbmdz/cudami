package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Person controller")
public class PersonController extends AbstractEntityController<Person> {

  private final PersonService service;
  private final WorkService workService;

  public PersonController(PersonService personService, WorkService workService) {
    this.service = personService;
    this.workService = workService;
  }

  @Operation(summary = "count all persons")
  @GetMapping(
      value = {
        "/v6/persons/count",
        "/v5/persons/count",
        "/v2/persons/count",
        "/latest/persons/count"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return service.count();
  }

  @Operation(summary = "Delete a person")
  @DeleteMapping(
      value = {"/v6/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the person") @PathVariable("uuid") UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "get all persons as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/persons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Person> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "get all persons born at given geo location")
  @GetMapping(
      value = {"/v6/persons/placeofbirth/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Person> findByGeoLocationOfBirth(
      @Parameter(
              example = "",
              description =
                  "UUID of the geo location of birth, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.findByGeoLocationOfBirth(GeoLocation.builder().uuid(uuid).build(), pageRequest);
  }

  @Operation(summary = "get all persons died at given geo location")
  @GetMapping(
      value = {"/v6/persons/placeofdeath/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Person> findByGeoLocationOfDeath(
      @Parameter(
              example = "",
              description =
                  "UUID of the geo location of death, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.findByGeoLocationOfDeath(GeoLocation.builder().uuid(uuid).build(), pageRequest);
  }

  @Override
  @Operation(
      summary = "Get a person by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/persons/identifier/**",
        "/v5/persons/identifier/**",
        "/v2/persons/identifier/**",
        "/latest/persons/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Person> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a person by namespace and id")
  @GetMapping(
      value = {
        "/v6/persons/identifier",
        "/v5/persons/identifier",
        "/v2/persons/identifier",
        "/latest/persons/identifier"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id,
      HttpServletRequest request)
      throws ServiceException {
    URI newLocation =
        URI.create(request.getRequestURI().concat(String.format("/%s:%s", namespace, id)));
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(newLocation).build();
  }

  @Operation(summary = "Get a person by uuid")
  @GetMapping(
      value = {
        "/v6/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Person> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the person, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
  }

  @Operation(summary = "Get a person's digital objects")
  @GetMapping(
      value = {
        "/v6/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v5/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v2/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/latest/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<DigitalObject> getDigitalObjects(@PathVariable("uuid") UUID uuid)
      throws ServiceException {
    return service.getDigitalObjects(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get languages of all persons")
  @GetMapping(
      value = {
        "/v6/persons/languages",
        "/v5/persons/languages",
        "/v3/persons/languages",
        "/latest/persons/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected EntityService<Person> getService() {
    return service;
  }

  @Operation(summary = "Get a person's works")
  @GetMapping(
      value = {
        "/v6/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/works",
        "/v5/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/works",
        "/v2/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/works",
        "/latest/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/works"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<Work> getWorks(@PathVariable("uuid") UUID uuid) throws ServiceException {
    return workService.getByPerson(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "save a newly created person")
  @PostMapping(
      value = {"/v6/persons", "/v5/persons", "/v2/persons", "/latest/persons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Person save(@RequestBody Person person, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(person, errors);
  }

  @Operation(summary = "update a person")
  @PutMapping(
      value = {
        "/v5/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v6/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Person update(
      @PathVariable("uuid") UUID uuid, @RequestBody Person person, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, person, errors);
  }
}
