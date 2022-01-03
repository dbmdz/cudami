package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.HeadwordEntryService;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
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
@Tag(name = "HeadwordEntry controller")
public class HeadwordEntryController {

  private final HeadwordEntryService headwordEntryService;

  public HeadwordEntryController(HeadwordEntryService headwordEntryService) {
    this.headwordEntryService = headwordEntryService;
  }

  @Operation(summary = "Get count of headwordentries")
  @GetMapping(
      value = {"/v5/headwordentries/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return headwordEntryService.count();
  }

  @Operation(summary = "Get all headwordentries")
  @GetMapping(
      value = {"/v5/headwordentries"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<HeadwordEntry> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest pageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return headwordEntryService.find(pageRequest);
  }

  @Operation(summary = "Get all headwordentries by headword")
  @GetMapping(
      value = {
        "/v5/headwordentries/headword/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HeadwordEntry> findByHeadword(
      @Parameter(
              example = "",
              description =
                  "UUID of the headword, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws IdentifiableServiceException {
    return headwordEntryService.findByHeadword(uuid);
  }

  @Operation(summary = "Get an headwordentry by namespace and id")
  @GetMapping(
      value = {"/v5/headwordentries/identifier/{namespace}:{id}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HeadwordEntry findByIdentifier(
      @Parameter(example = "", description = "Namespace of the identifier")
          @PathVariable("namespace")
          String namespace,
      @Parameter(example = "", description = "value of the identifier") @PathVariable("id")
          String id)
      throws IdentifiableServiceException {
    return headwordEntryService.getByIdentifier(namespace, id);
  }

  @Operation(summary = "Get an headwordentry")
  @GetMapping(
      value = {
        "/v5/headwordentries/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HeadwordEntry> findByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the headwordentry, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    HeadwordEntry headwordEntry;
    if (pLocale == null) {
      headwordEntry = headwordEntryService.get(uuid);
    } else {
      headwordEntry = headwordEntryService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(headwordEntry, HttpStatus.OK);
  }

  @Operation(summary = "Get languages of all headwordentries")
  @GetMapping(
      value = {"/v5/headwordentries/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return this.headwordEntryService.getLanguages();
  }

  @Operation(summary = "Save a newly created headwordentry")
  @PostMapping(
      value = {"/v5/headwordentries"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HeadwordEntry save(@RequestBody HeadwordEntry headwordEntry, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return headwordEntryService.save(headwordEntry);
  }

  @Operation(summary = "Update an headwordentry")
  @PutMapping(
      value = {"/v5/headwordentries/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HeadwordEntry update(
      @PathVariable UUID uuid, @RequestBody HeadwordEntry headwordEntry, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, headwordEntry.getUuid());
    return headwordEntryService.update(headwordEntry);
  }
}
