package de.digitalcollections.cudami.server.controller.legal;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.legal.LicenseService;
import de.digitalcollections.cudami.server.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.editor.UrlEditor;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "License controller")
public class LicenseController extends AbstractUniqueObjectController<License> {

  private final LicenseService service;

  public LicenseController(LicenseService service) {
    this.service = service;
  }

  @Operation(summary = "Get count of licenses")
  @GetMapping(
      value = {"/v6/licenses/count", "/v5/licenses/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return super.count();
  }

  @Operation(summary = "Delete license by given url")
  @DeleteMapping(
      value = {"/v6/licenses", "/v5/licenses"},
      params = "url",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteByUrl(@RequestParam(name = "url", required = true) URL url)
      throws ServiceException {
    // WARNING: a DELETE request with param seems not to be spec allowed?
    // an url as path variable is technically not possible (unescaping leads to not
    // allowed
    // characters in url)
    service.deleteByUrl(url);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(summary = "Delete a license by uuid")
  @DeleteMapping(
      value = {
        "/v6/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteByUuid(
      @Parameter(example = "", description = "UUID of the license") @PathVariable("uuid") UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Delete licenses by given uuid list")
  @DeleteMapping(
      value = {"/v6/licenses", "/v5/licenses"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteByUuids(@RequestBody List<UUID> uuids)
      throws ConflictException, ServiceException {
    // WARNING: a DELETE request with body seems not to be spec allowed?
    // FIXME: How to implement deleteByUrl (also with body? how to distinguish these
    // both methods?
    // give param?)
    return super.delete(uuids);
  }

  // No need for a v5 controller, since the v5 url were never actually used
  @Operation(summary = "Get all licenses as (filtered, sorted, paged) list")
  @GetMapping(
      value = {"/v6/licenses"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<License> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get all licenses in reduced form")
  @GetMapping(
      value = {"/v6/licenses/all", "/v5/licenses/all"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<License> getAll() throws ServiceException {
    return service.getAll();
  }

  @Operation(summary = "Get a license by url")
  @GetMapping(
      value = {"/v6/licenses", "/v5/licenses"},
      params = "url",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<License> getByUrl(@RequestParam(name = "url", required = true) URL url)
      throws MalformedURLException, ServiceException {
    License license = service.getByUrl(url);
    return new ResponseEntity<>(license, license != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get a license by uuid")
  @GetMapping(
      value = {
        "/v6/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<License> getByUuid(@PathVariable UUID uuid) throws ServiceException {
    return super.getByUuid(uuid);
  }

  @Operation(summary = "Get languages of all licenses")
  @GetMapping(
      value = {"/v6/licenses/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return service.getLanguages();
  }

  @Override
  protected UniqueObjectService<License> getService() {
    return service;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(URL.class, new UrlEditor());
  }

  @Operation(summary = "Save a newly created license")
  @PostMapping(
      value = {"/v6/licenses", "/v5/licenses"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public License save(@RequestBody License license, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(license, errors);
  }

  @Operation(summary = "Update a license")
  @PutMapping(
      value = {
        "/v6/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public License update(
      @Parameter(example = "", description = "UUID of the license") @PathVariable("uuid") UUID uuid,
      @RequestBody License license,
      BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, license, errors);
  }
}
