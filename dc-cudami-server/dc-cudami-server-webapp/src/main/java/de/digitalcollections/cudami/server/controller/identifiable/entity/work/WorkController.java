package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
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
@Api(description = "The Work controller", name = "Work controller")
public class WorkController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkController.class);

  @Autowired WorkService service;

  @Autowired LocaleService localeService;

  @ApiMethod(description = "count all works")
  @GetMapping(
      value = {"/latest/works/count", "/v2/works/count"},
      produces = "application/json")
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "get all works")
  @GetMapping(
      value = {"/latest/works", "/v2/works"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<Work> findAll(
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
          "get a work as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/works/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/works/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Work> get(
      @ApiPathParam(
              description = "UUID of the work, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Work result;
    if (pLocale == null) {
      result = service.get(uuid);
    } else {
      result = service.get(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "get a work as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/works/identifier", "/v2/works/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Work> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    Work result = service.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(description = "save a newly created work")
  @PostMapping(
      value = {"/latest/works", "/v2/works"},
      produces = "application/json")
  @ApiResponseObject
  public Work save(@RequestBody Work work, BindingResult errors)
      throws IdentifiableServiceException {
    return service.save(work);
  }

  @ApiMethod(description = "update a work")
  @PutMapping(
      value = {"/latest/works/{uuid}", "/v2/works/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public Work update(@PathVariable("uuid") UUID uuid, @RequestBody Work work, BindingResult errors)
      throws IdentifiableServiceException {
    if (uuid == null || work == null || !uuid.equals(work.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing work");
    }

    return service.update(work);
  }

  @ApiMethod(description = "Get creators of a work")
  @GetMapping(
      value = {"/latest/works/{uuid}/creators", "/v2/works/{uuid}/creators"},
      produces = "application/json")
  @ApiResponseObject
  public List<Agent> getCreators(@PathVariable UUID uuid) {
    return service.getCreators(uuid);
  }

  @ApiMethod(description = "Get items of a work")
  @GetMapping(
      value = {"/latest/works/{uuid}/items", "/v2/works/{uuid}/items"},
      produces = "application/json")
  @ApiResponseObject
  public List<Item> getItems(@PathVariable UUID uuid) {
    return service.getItems(uuid);
  }
}
