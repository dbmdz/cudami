package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
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
@Tag(name = "Corporate body controller")
public class CorporateBodyController extends AbstractIdentifiableController<CorporateBody> {

  private static final Pattern GNDID_PATTERN = Pattern.compile("(\\d+(-.)?)|(\\d+X)");

  private final CorporateBodyService corporateBodyService;

  public CorporateBodyController(CorporateBodyService corporateBodyservice) {
    this.corporateBodyService = corporateBodyservice;
  }

  @Override
  protected IdentifiableService<CorporateBody> getService() {
    return corporateBodyService;
  }

  @Operation(summary = "Fetch a corporate body by GND-ID from external system and save it")
  @PostMapping(
      value = {
        "/v6/corporatebodies/gnd/{gndId}",
        "/v5/corporatebodies/gnd/{gndId}",
        "/v3/corporatebodies/gnd/{gndId}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public CorporateBody fetchAndSaveByGndId(
      @Parameter(
              example = "",
              description = "GND-ID of the corporate body, e.g. <tt>2007744-0</tt>")
          @PathVariable("gndId")
          String gndId)
      throws IdentifiableServiceException {
    if (!GNDID_PATTERN.matcher(gndId).matches()) {
      throw new IllegalArgumentException("Invalid GND ID: " + gndId);
    }
    return corporateBodyService.fetchAndSaveByGndId(gndId);
  }

  @Operation(summary = "Get all corporate bodies")
  @GetMapping(
      value = {"/v6/corporatebodies"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<CorporateBody> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return corporateBodyService.find(searchPageRequest);
  }

  @Operation(
      summary = "Get a corporate body by namespace and id",
      description =
          "Separate namespace and id with a colon, d.h. foo:bar. It is also possible, to a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/corporatebodies/identifier/**",
        "/v5/corporatebodies/identifier/**",
        "/v3/corporatebodies/identifier/**",
        "/latest/corporatebodies/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CorporateBody> getByIdentifier(HttpServletRequest request)
      throws IdentifiableServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get corporate body by refId")
  @GetMapping(
      value = {
        "/v6/corporatebodies/{refId:[0-9]+}",
        "/v5/corporatebodies/{refId:[0-9]+}",
        "/v3/corporatebodies/{refId:[0-9]+}",
        "/latest/corporatebodies/{refId:[0-9]+}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public CorporateBody getByRefId(
      @Parameter(example = "", description = "reference id") @PathVariable("refId") long refId)
      throws IdentifiableServiceException {
    return corporateBodyService.getByRefId(refId);
  }

  @Operation(summary = "Get a corporate body by uuid")
  @GetMapping(
      value = {
        "/v6/corporatebodies/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v5/corporatebodies/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/corporatebodies/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/latest/corporatebodies/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CorporateBody> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the corporate body, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    CorporateBody corporateBody;
    if (pLocale == null) {
      corporateBody = corporateBodyService.getByUuid(uuid);
    } else {
      corporateBody = corporateBodyService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(corporateBody, HttpStatus.OK);
  }

  @Operation(summary = "Get languages of all corporatebodies")
  @GetMapping(
      value = {
        "/v6/corporatebodies/languages",
        "/v5/corporatebodies/languages",
        "/v3/corporatebodies/languages",
        "/latest/corporatebodies/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return corporateBodyService.getLanguages();
  }

  @Operation(summary = "Save a newly created corporate body")
  @PostMapping(
      value = {
        "/v6/corporatebodies",
        "/v5/corporatebodies",
        "/v2/corporatebodies",
        "/latest/corporatebodies"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public CorporateBody save(@RequestBody CorporateBody corporateBody, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return corporateBodyService.save(corporateBody);
  }

  @Operation(summary = "Update a corporate body")
  @PutMapping(
      value = {
        "/v6/corporatebodies/{uuid}",
        "/v5/corporatebodies/{uuid}",
        "/v2/corporatebodies/{uuid}",
        "/latest/corporatebodies/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public CorporateBody update(
      @Parameter(
              example = "",
              description =
                  "UUID of the corporate body, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestBody CorporateBody corporateBody,
      BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, corporateBody.getUuid());
    return corporateBodyService.update(corporateBody);
  }
}
