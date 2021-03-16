package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api(description = "The Person controller", name = "Person controller")
public class PersonController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class);

  @Autowired LocaleService localeService;
  @Autowired PersonService personService;

  @ApiMethod(description = "count all persons")
  @GetMapping(
      value = {"/latest/persons/count", "/v2/persons/count"},
      produces = "application/json")
  @ApiResponseObject
  public long count() {
    return personService.count();
  }

  @ApiMethod(description = "get all persons")
  @GetMapping(
      value = {"/latest/persons", "/v2/persons"},
      produces = "application/json")
  @ApiResponseObject
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

  @ApiMethod(description = "get all persons born at given geo location")
  @GetMapping(
      value = {"/latest/persons/placeofbirth/{uuid}", "/v2/persons/placeofbirth/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<Person> getByPlaceOfBirth(
      @ApiPathParam(
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

  @ApiMethod(description = "get all persons died at given geo location")
  @GetMapping(
      value = {"/latest/persons/placeofdeath/{uuid}", "/v2/persons/placeofdeath/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<Person> getByPlaceOfDeath(
      @ApiPathParam(
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

  @ApiMethod(
      description =
          "get a person as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/persons/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/persons/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Person> get(
      @ApiPathParam(
              description =
                  "UUID of the person, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
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

  @ApiMethod(
      description =
          "get a person as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/persons/identifier", "/v2/persons/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Person> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    Person result = personService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "get a person's digital objects as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/persons/{uuid}/digitalobjects", "/v2/persons/{uuid}/digitalobjects"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ApiResponseObject
  public Set<DigitalObject> getDigitalObjects(@PathVariable("uuid") UUID uuid)
      throws IdentifiableServiceException {
    return personService.getDigitalObjects(uuid);
  }

  @ApiMethod(description = "Get languages of all persons")
  @GetMapping(
      value = {"/latest/persons/languages", "/v3/persons/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Locale> getLanguages() {
    return personService.getLanguages();
  }

  @ApiMethod(
      description =
          "get a person's works as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/persons/{uuid}/works", "/v2/persons/{uuid}/works"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ApiResponseObject
  public Set<Work> getWorks(@PathVariable("uuid") UUID uuid) throws IdentifiableServiceException {
    return personService.getWorks(uuid);
  }

  @ApiMethod(description = "save a newly created person")
  @PostMapping(
      value = {"/latest/persons", "/v2/persons"},
      produces = "application/json")
  @ApiResponseObject
  public Person save(@RequestBody Person person, BindingResult errors)
      throws IdentifiableServiceException {
    return personService.save(person);
  }

  @ApiMethod(description = "update a person")
  @PutMapping(
      value = {"/latest/persons/{uuid}", "/v2/persons/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public Person update(
      @PathVariable("uuid") UUID uuid, @RequestBody Person person, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, person.getUuid());
    return personService.update(person);
  }
}
