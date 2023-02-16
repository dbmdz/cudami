package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
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
@Tag(name = "Manifestation controller")
public class ManifestationController extends AbstractIdentifiableController<Manifestation> {

  private ItemService itemService;
  private ManifestationService service;

  public ManifestationController(
      ManifestationService manifestationService, ItemService itemService) {
    service = manifestationService;
    this.itemService = itemService;
  }

  @Override
  protected IdentifiableService<Manifestation> getService() {
    return service;
  }

  @Operation(summary = "Count all manifestations")
  @GetMapping(
      value = {"/v6/manifestations/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return service.count();
  }

  @Operation(summary = "Delete a manifestation")
  @DeleteMapping(
      value = {"/v6/manifestations/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the manifestation") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = service.delete(uuid);
    } catch (ServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all manifestations")
  @GetMapping(
      value = {"/v6/manifestations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Manifestation> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage,
      @RequestParam(name = "parent.uuid", required = false)
          FilterCriterion<UUID> parentUuidFilterCriterion) {
    return super.find(
        pageNumber,
        pageSize,
        sortBy,
        searchTerm,
        labelTerm,
        labelLanguage,
        Pair.of("parent.uuid", parentUuidFilterCriterion));
  }

  @Operation(summary = "Find all children of a manifestation")
  @GetMapping(
      value = {"/v6/manifestations/{uuid}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Manifestation> findSubcollections(
      @Parameter(example = "", description = "UUID of the manifestation") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.findChildren(uuid, pageRequest);
  }

  @Operation(summary = "Find all items of a manifestation")
  @GetMapping(
      value = {"/v6/manifestations/{uuid}/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> findItems(
      @Parameter(example = "", description = "UUID of the manifestation") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return itemService.findItemsByManifestation(uuid, pageRequest);
  }

  @Operation(
      summary = "Get a manifestation by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/manifestations/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<Manifestation> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a manifestation by uuid")
  @GetMapping(
      value = {"/v6/manifestations/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Manifestation> getByUuid(
      @Parameter(
              name = "uuid",
              description =
                  "UUID of the manifestation, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {

    Manifestation result;
    if (pLocale == null) {
      result = service.getByUuid(uuid);
    } else {
      result = service.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get languages of all manifestations")
  @GetMapping(
      value = {"/v6/manifestations/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return service.getLanguages();
  }

  @Operation(
      summary = "Get languages of all items",
      description = "Get languages of all items",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/manifestations/{uuid}/items/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfItems(
      @Parameter(name = "uuid", description = "UUID of the manifestation") @PathVariable
          UUID uuid) {
    return itemService.getLanguagesOfItemsForManifestation(uuid);
  }

  @Operation(summary = "Save a newly created manifestation")
  @PostMapping(
      value = {"/v6/manifestations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Manifestation save(@RequestBody Manifestation manifestation, BindingResult errors)
      throws ServiceException, ValidationException {
    service.save(manifestation);
    return manifestation;
  }

  @Operation(summary = "update an manifestation")
  @PutMapping(
      value = {"/v6/manifestations/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Manifestation update(
      @PathVariable UUID uuid, @RequestBody Manifestation manifestation, BindingResult errors)
      throws ServiceException, ValidationException {
    if (uuid == null || manifestation == null || !uuid.equals(manifestation.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing manifestation");
    }

    service.update(manifestation);
    return manifestation;
  }
}
