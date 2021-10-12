package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Person controller")
public class PersonController {
  private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class);

  private final PersonService personService;

  public PersonController(PersonService personService) {
    this.personService = personService;
  }

  @Operation(summary = "count all persons")
  @GetMapping(
      value = {"/v5/persons/count", "/v2/persons/count", "/latest/persons/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return personService.count();
  }

  @Operation(summary = "get all persons")
  @GetMapping(
      value = {"/v5/persons", "/v2/persons", "/latest/persons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Person> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false, defaultValue = "de") String language,
      @RequestParam(name = "initial", required = false) String initial,
      @RequestParam(name = "previewImage", required = false)
          FilterCriterion<UUID> previewImageFilter,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }

    if (previewImageFilter != null) {
      Filtering filtering =
          Filtering.defaultBuilder().add("previewImage", previewImageFilter).build();
      searchPageRequest.setFiltering(filtering);
    }
    if (initial == null) {
      return personService.find(searchPageRequest);
    }
    return personService.findByLanguageAndInitial(searchPageRequest, language, initial);
  }

  @Operation(summary = "get all persons born at given geo location")
  @GetMapping(
      value = {
        "/v5/persons/placeofbirth/{uuid}",
        "/v2/persons/placeofbirth/{uuid}",
        "/latest/persons/placeofbirth/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Person> getByPlaceOfBirth(
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
    return personService.findByLocationOfBirth(pageRequest, uuid);
  }

  @Operation(summary = "get all persons died at given geo location")
  @GetMapping(
      value = {
        "/v5/persons/placeofdeath/{uuid}",
        "/v2/persons/placeofdeath/{uuid}",
        "/latest/persons/placeofdeath/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Person> getByPlaceOfDeath(
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
    return personService.findByLocationOfDeath(pageRequest, uuid);
  }

  @Operation(summary = "Get a person by uuid")
  @GetMapping(
      value = {
        "/v5/persons/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/persons/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/latest/persons/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Person> get(
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
      throws IdentifiableServiceException {

    Person result;
    if (pLocale == null) {
      result = personService.get(uuid);
    } else {
      result = personService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get a person by namespace and id")
  @GetMapping(
      value = {"/v5/persons/identifier", "/v2/persons/identifier", "/latest/persons/identifier"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Person> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    Person result = personService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get a person's digital objects")
  @GetMapping(
      value = {
        "/v5/persons/{uuid}/digitalobjects",
        "/v2/persons/{uuid}/digitalobjects",
        "/latest/persons/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<DigitalObject> getDigitalObjects(@PathVariable("uuid") UUID uuid)
      throws IdentifiableServiceException {
    return personService.getDigitalObjects(uuid);
  }

  @Operation(summary = "Get languages of all persons")
  @GetMapping(
      value = {"/v5/persons/languages", "/v3/persons/languages", "/latest/persons/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return personService.getLanguages();
  }

  @Operation(summary = "Get a person's works")
  @GetMapping(
      value = {
        "/v5/persons/{uuid}/works",
        "/v2/persons/{uuid}/works",
        "/latest/persons/{uuid}/works"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<Work> getWorks(@PathVariable("uuid") UUID uuid) throws IdentifiableServiceException {
    return personService.getWorks(uuid);
  }

  @Operation(summary = "save a newly created person")
  @PostMapping(
      value = {"/v5/persons", "/v2/persons", "/latest/persons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Person save(@RequestBody Person person, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return personService.save(person);
  }

  @Operation(summary = "update a person")
  @PutMapping(
      value = {"/v5/persons/{uuid}", "/v2/persons/{uuid}", "/latest/persons/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Person update(
      @PathVariable("uuid") UUID uuid, @RequestBody Person person, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, person.getUuid());
    return personService.update(person);
  }
}
