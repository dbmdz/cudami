package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
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
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
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
@Tag(name = "Item controller")
public class ItemController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @Operation(summary = "Add digital object to an item")
  @PostMapping(
      value = {
        "/latest/items/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v2/items/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public boolean addDigitalObject(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid,
      @Parameter(name = "digitalObjectUuid", description = "UUID of the digital object")
          @PathVariable
          UUID digitalObjectUuid) {
    return itemService.addDigitalObject(uuid, digitalObjectUuid);
  }

  @Operation(summary = "Add work to an item")
  @PostMapping(
      value = {"/latest/items/{uuid}/works/{workUuid}", "/v2/items/{uuid}/works/{workUuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public boolean addWork(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid,
      @Parameter(name = "workUuid", description = "UUID of the work") @PathVariable UUID workUuid) {
    return itemService.addWork(uuid, workUuid);
  }

  @Operation(summary = "count all items")
  @GetMapping(
      value = {"/v5/items/count", "/v2/items/count", "/latest/items/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return itemService.count();
  }

  @Operation(summary = "get all items")
  @GetMapping(
      value = {"/v5/items", "/v2/items", "/latest/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> find(
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
      return itemService.find(pageRequest);
    }
    return itemService.findByLanguageAndInitial(pageRequest, language, initial);
  }

  @Operation(summary = "Get an item by namespace and id")
  @GetMapping(
      value = {
        "/v5/items/identifier/{namespace}:{id}", "/v5/items/identifier/{namespace}:{id}.json",
        "/v2/items/identifier/{namespace}:{id}", "/v2/items/identifier/{namespace}:{id}.json",
        "/latest/items/identifier/{namespace}:{id}",
            "/latest/items/identifier/{namespace}:{id}.json"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Item> getByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {
    Item result = itemService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get an item by namespace and id")
  @GetMapping(
      value = {"/v5/items/identifier", "/v2/items/identifier", "/latest/items/identifier"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id,
      HttpServletRequest request)
      throws IdentifiableServiceException {
    URI newLocation =
        URI.create(request.getRequestURI().concat(String.format("/%s:%s", namespace, id)));
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(newLocation).build();
  }

  @Operation(summary = "Get an item by uuid")
  @GetMapping(
      value = {"/v5/items/{uuid}", "/v2/items/{uuid}", "/latest/items/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Item> getByUuid(
      @Parameter(
              name = "uuid",
              description = "UUID of the item, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Item result;
    if (pLocale == null) {
      result = itemService.getByUuid(uuid);
    } else {
      result = itemService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get digital objects of this item")
  @GetMapping(
      value = {"/latest/items/{uuid}/digitalobjects", "/v2/items/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<DigitalObject> getDigitalObjects(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return itemService.getDigitalObjects(uuid);
  }

  @Operation(summary = "Get works embodied in an item")
  @GetMapping(
      value = {"/latest/items/{uuid}/works", "/v2/items/{uuid}/works"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<Work> getWorks(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return itemService.getWorks(uuid);
  }

  @Operation(summary = "save a newly created item")
  @PostMapping(
      value = {"/v5/items", "/v2/items", "/latest/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item save(@RequestBody Item item, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return itemService.save(item);
  }

  @Operation(summary = "update an item")
  @PutMapping(
      value = {"/v5/items/{uuid}", "/v2/items/{uuid}", "/latest/items/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item update(@PathVariable("uuid") UUID uuid, @RequestBody Item item, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    if (uuid == null || item == null || !uuid.equals(item.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing item");
    }

    return itemService.update(item);
  }
}
