package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.HeadwordEntryService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
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
public class HeadwordEntryController extends AbstractIdentifiableController<HeadwordEntry> {

  private final HeadwordEntryService headwordEntryService;

  public HeadwordEntryController(HeadwordEntryService headwordEntryService) {
    this.headwordEntryService = headwordEntryService;
  }

  @Override
  protected IdentifiableService<HeadwordEntry> getService() {
    return headwordEntryService;
  }

  @Operation(summary = "Get count of headwordentries")
  @GetMapping(
      value = {"/v6/headwordentries/count", "/v5/headwordentries/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return headwordEntryService.count();
  }

  @Operation(summary = "Get all headwordentries")
  @GetMapping(
      value = {"/v6/headwordentries"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<HeadwordEntry> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage) {
    return super.find(pageNumber, pageSize, sortBy, searchTerm, labelTerm, labelLanguage);
  }

  @Operation(summary = "Get all headwordentries by headword")
  @GetMapping(
      value = {
        "/v6/headwordentries/headword/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v5/headwordentries/headword/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HeadwordEntry> getByHeadword(
      @Parameter(
              example = "",
              description =
                  "UUID of the headword, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws IdentifiableServiceException {
    return headwordEntryService.getByHeadword(uuid);
  }

  @Override
  @Operation(
      summary = "Get a headword entry by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/headwordentries/identifier/**", "/v5/headwordentries/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HeadwordEntry> getByIdentifier(HttpServletRequest request)
      throws IdentifiableServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get an headwordentry")
  @GetMapping(
      value = {
        "/v6/headwordentries/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v5/headwordentries/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HeadwordEntry> getByUuid(
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
      headwordEntry = headwordEntryService.getByUuid(uuid);
    } else {
      headwordEntry = headwordEntryService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(headwordEntry, HttpStatus.OK);
  }

  @Operation(summary = "Get languages of all headwordentries")
  @GetMapping(
      value = {"/v6/headwordentries/languages", "/v5/headwordentries/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return this.headwordEntryService.getLanguages();
  }

  @Operation(summary = "Save a newly created headwordentry")
  @PostMapping(
      value = {"/v6/headwordentries", "/v5/headwordentries"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HeadwordEntry save(@RequestBody HeadwordEntry headwordEntry, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return headwordEntryService.save(headwordEntry);
  }

  @Operation(summary = "Update an headwordentry")
  @PutMapping(
      value = {"/v6/headwordentries/{uuid}", "/v5/headwordentries/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HeadwordEntry update(
      @PathVariable UUID uuid, @RequestBody HeadwordEntry headwordEntry, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, headwordEntry.getUuid());
    return headwordEntryService.update(headwordEntry);
  }
}
