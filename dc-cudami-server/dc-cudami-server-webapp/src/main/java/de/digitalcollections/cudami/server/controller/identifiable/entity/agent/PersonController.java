package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Person controller")
public class PersonController extends AbstractIdentifiableController<Person> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class);

  private final PersonService personService;
  private final WorkService workService;

  public PersonController(PersonService personService, WorkService workService) {
    this.personService = personService;
    this.workService = workService;
  }

  @Override
  protected IdentifiableService<Person> getService() {
    return personService;
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
  public long count() {
    return personService.count();
  }

  @Operation(summary = "Delete a person")
  @DeleteMapping(
      value = {"/v6/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the person") @PathVariable("uuid") UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = personService.delete(uuid);
    } catch (ServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "get all persons as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/persons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Person> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria) {
    PageRequest pageRequest =
        createPageRequest(Person.class, pageNumber, pageSize, sortBy, filterCriteria);
    return personService.find(pageRequest);
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
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return personService.findByGeoLocationOfBirth(pageRequest, uuid);
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
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return personService.findByGeoLocationOfDeath(pageRequest, uuid);
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

    Person result;
    if (pLocale == null) {
      result = personService.getByUuid(uuid);
    } else {
      result = personService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
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
    return personService.getDigitalObjects(uuid);
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
  public List<Locale> getLanguages() {
    return personService.getLanguages();
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
    return workService.getForPerson(uuid);
  }

  @Operation(summary = "save a newly created person")
  @PostMapping(
      value = {"/v6/persons", "/v5/persons", "/v2/persons", "/latest/persons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Person save(@RequestBody Person person, BindingResult errors)
      throws ServiceException, ValidationException {
    personService.save(person);
    return person;
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
    assert Objects.equals(uuid, person.getUuid());
    personService.update(person);
    return person;
  }
}
