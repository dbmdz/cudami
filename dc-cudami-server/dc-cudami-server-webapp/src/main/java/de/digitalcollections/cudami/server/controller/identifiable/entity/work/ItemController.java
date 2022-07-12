package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Item controller")
public class ItemController extends AbstractIdentifiableController<Item> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @Override
  protected IdentifiableService<Item> getService() {
    return itemService;
  }

  @Operation(summary = "Add digital object to an item")
  @PostMapping(
      value = {
        "/v6/items/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v2/items/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/latest/items/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public boolean addDigitalObject(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid,
      @Parameter(name = "digitalObjectUuid", description = "UUID of the digital object")
          @PathVariable
          UUID digitalObjectUuid)
      throws ValidationException, ConflictException, IdentifiableServiceException {
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
      value = {"/v6/items/count", "/v5/items/count", "/v2/items/count", "/latest/items/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return itemService.count();
  }

  @Operation(summary = "get all items")
  @GetMapping(
      value = {"/v6/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage,
      @RequestParam(name = "part_of_item.uuid", required = false)
          FilterCriterion<UUID> partOfItemUuidFilterCriterion) {
    return super.find(
        pageNumber,
        pageSize,
        sortBy,
        searchTerm,
        labelTerm,
        labelLanguage,
        "part_of_item.uuid",
        partOfItemUuidFilterCriterion);
  }

  @Operation(
      summary = "Get an item by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/items/identifier/**",
        "/v5/items/identifier/**",
        "/v2/items/identifier/**",
        "/latest/items/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<Item> getByIdentifier(HttpServletRequest request)
      throws IdentifiableServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get an item by namespace and id")
  @GetMapping(
      value = {
        "/v6/items/identifier",
        "/v5/items/identifier",
        "/v2/items/identifier",
        "/latest/items/identifier"
      },
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
      value = {"/v6/items/{uuid}"},
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
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get digital objects of this item")
  @GetMapping(
      value = {
        "/v6/items/{uuid}/digitalobjects",
        "/v2/items/{uuid}/digitalobjects",
        "/latest/items/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<DigitalObject> getDigitalObjects(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return itemService.getDigitalObjects(uuid);
  }

  @Operation(summary = "Get works embodied in an item")
  @GetMapping(
      value = {"/v6/items/{uuid}/works", "/v2/items/{uuid}/works", "/latest/items/{uuid}/works"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<Work> getWorks(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return itemService.getWorks(uuid);
  }

  @Operation(summary = "save a newly created item")
  @PostMapping(
      value = {"/v6/items", "/v5/items", "/v2/items", "/latest/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item save(@RequestBody Item item, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return itemService.save(item);
  }

  @Operation(summary = "update an item")
  @PutMapping(
      value = {"/v6/items/{uuid}", "/v5/items/{uuid}", "/v2/items/{uuid}", "/latest/items/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item update(@PathVariable("uuid") UUID uuid, @RequestBody Item item, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    if (uuid == null || item == null || !uuid.equals(item.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing item");
    }

    return itemService.update(item);
  }

  @Operation(summary = "Delete an item")
  @DeleteMapping(
      value = {"/v6/items/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the item") @PathVariable("uuid") UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = itemService.delete(uuid);
    } catch (IdentifiableServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
