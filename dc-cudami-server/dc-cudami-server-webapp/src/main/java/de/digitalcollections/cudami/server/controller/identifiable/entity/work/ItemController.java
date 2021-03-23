package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
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
@Api(description = "The Item controller", name = "Item controller")
public class ItemController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

  private final ItemService service;

  public ItemController(ItemService service) {
    this.service = service;
  }

  @ApiMethod(description = "count all items")
  @GetMapping(
      value = {"/latest/items/count", "/v2/items/count"},
      produces = "application/json")
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "get all items")
  @GetMapping(
      value = {"/latest/items", "/v2/items"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<Item> findAll(
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
          "get an item as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/items/{uuid}", "/v2/items/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Item> get(
      @ApiPathParam(
              name = "uuid",
              description = "UUID of the item, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Item result;
    if (pLocale == null) {
      result = service.get(uuid);
    } else {
      result = service.get(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "get an item as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/items/identifier", "/v2/items/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Item> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    Item result = service.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(description = "save a newly created item")
  @PostMapping(
      value = {"/latest/items", "/v2/items"},
      produces = "application/json")
  @ApiResponseObject
  public Item save(@RequestBody Item item, BindingResult errors)
      throws IdentifiableServiceException {
    return service.save(item);
  }

  @ApiMethod(description = "update an item")
  @PutMapping(
      value = {"/latest/items/{uuid}", "/v2/items/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public Item update(@PathVariable("uuid") UUID uuid, @RequestBody Item item, BindingResult errors)
      throws IdentifiableServiceException {
    if (uuid == null || item == null || !uuid.equals(item.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing item");
    }

    return service.update(item);
  }

  @ApiMethod(description = "Get digital objects of this item")
  @GetMapping(
      value = {"/latest/items/{uuid}/digitalobjects", "/v2/items/{uuid}/digitalobjects"},
      produces = "application/json")
  @ApiResponseObject
  public Set<DigitalObject> getDigitalObjects(
      @ApiPathParam(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return service.getDigitalObjects(uuid);
  }

  @ApiMethod(description = "Get works embodied in an item")
  @GetMapping(
      value = {"/latest/items/{uuid}/works", "/v2/items/{uuid}/works"},
      produces = "application/json")
  @ApiResponseObject
  public Set<Work> getWorks(
      @ApiPathParam(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return service.getWorks(uuid);
  }

  @ApiMethod(description = "Add work to an item")
  @PostMapping(
      value = {"/latest/items/{uuid}/works/{workUuid}", "/v2/items/{uuid}/works/{workUuid}"},
      produces = "application/json")
  @ApiResponseObject
  public boolean addWork(
      @ApiPathParam(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid,
      @ApiPathParam(name = "workUuid", description = "UUID of the work") @PathVariable
          UUID workUuid) {
    return service.addWork(uuid, workUuid);
  }

  @ApiMethod(description = "Add digital object to an item")
  @PostMapping(
      value = {
        "/latest/items/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v2/items/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = "application/json")
  @ApiResponseObject
  public boolean addDigitalObject(
      @ApiPathParam(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid,
      @ApiPathParam(name = "digitalObjectUuid", description = "UUID of the digital object")
          @PathVariable
          UUID digitalObjectUuid) {
    return service.addDigitalObject(uuid, digitalObjectUuid);
  }
}
