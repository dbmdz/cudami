package de.digitalcollections.cudami.server.controller.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.GivenNameService;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
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
@Api(description = "The GivenName controller", name = "GivenName controller")
public class GivenNameController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GivenNameController.class);

  private final GivenNameService service;

  public GivenNameController(GivenNameService service) {
    this.service = service;
  }

  @ApiMethod(description = "get all given names")
  @GetMapping(
      value = {"/latest/givennames", "/v2/givennames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<GivenName> findAll(
      Pageable pageable,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false, defaultValue = "de") String language,
      @RequestParam(name = "initial", required = false) String initial) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (initial == null) {
      return service.find(pageRequest);
    }
    return service.findByLanguageAndInitial(pageRequest, language, initial);
  }

  @ApiMethod(
      description =
          "get a givenname as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/givennames/{uuid}", "/v2/givennames/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<GivenName> get(
      @ApiPathParam(
              description =
                  "UUID of the givenname, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    GivenName result;
    if (pLocale == null) {
      result = service.get(uuid);
    } else {
      result = service.get(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "get a givenname as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/givennames/identifier", "/v2/givennames/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<GivenName> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    GivenName result = service.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(description = "save a newly created givenname")
  @PostMapping(
      value = {"/latest/givennames", "/v2/givennames"},
      produces = "application/json")
  @ApiResponseObject
  public GivenName save(@RequestBody GivenName givenName, BindingResult errors)
      throws IdentifiableServiceException {
    return service.save(givenName);
  }

  @ApiMethod(description = "update a givenname")
  @PutMapping(
      value = {"/latest/givennames/{uuid}", "/v2/givennames/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public GivenName update(
      @PathVariable("uuid") UUID uuid, @RequestBody GivenName givenName, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, givenName.getUuid());
    return service.update(givenName);
  }
}
