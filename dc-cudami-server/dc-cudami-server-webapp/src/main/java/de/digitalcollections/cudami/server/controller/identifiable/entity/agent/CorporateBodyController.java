package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
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
@Api(description = "The corporate body controller", name = "Corporate body controller")
public class CorporateBodyController {

  private static final Pattern GNDID_PATTERN = Pattern.compile("(\\d+(-.)?)|(\\d+X)");

  private final CorporateBodyService corporateBodyService;

  @Autowired
  public CorporateBodyController(CorporateBodyService corporateBodyService) {
    this.corporateBodyService = corporateBodyService;
  }

  @ApiMethod(description = "Fetch a corporate body by GND-ID from external system and save it")
  @PostMapping(
      value = {"/latest/corporatebodies/gnd/{gndId}", "/v3/corporatebodies/gnd/{gndId}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public CorporateBody fetchAndSaveByGndId(
      @ApiPathParam(description = "GND-ID of the corporate body, e.g. <tt>2007744-0</tt>")
          @PathVariable("gndId")
          String gndId)
      throws IdentifiableServiceException {
    if (!GNDID_PATTERN.matcher(gndId).matches()) {
      throw new IllegalArgumentException("Invalid GND ID: " + gndId);
    }
    return corporateBodyService.fetchAndSaveByGndId(gndId);
  }

  @ApiMethod(description = "Get all corporate bodies")
  @GetMapping(
      value = {"/latest/corporatebodies", "/v2/corporatebodies"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<CorporateBody> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return corporateBodyService.find(searchPageRequest);
  }

  // Test-URL: http://localhost:9000/latest/corporatebodies/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get an corporate body as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/corporatebodies/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/corporatebodies/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<CorporateBody> get(
      @ApiPathParam(
              description =
                  "UUID of the corporate body, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    CorporateBody corporateBody;
    if (pLocale == null) {
      corporateBody = corporateBodyService.get(uuid);
    } else {
      corporateBody = corporateBodyService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(corporateBody, HttpStatus.OK);
  }

  @ApiMethod(description = "Get corporate body by namespace and id")
  @GetMapping(
      value = {
        "/latest/corporatebodies/identifier/{namespace}:{id}",
        "/v3/corporatebodies/identifier/{namespace}:{id}"
      },
      produces = "application/json")
  @ApiResponseObject
  public CorporateBody getByIdentifier(
      @ApiPathParam(description = "namespace of identifier") @PathVariable("namespace")
          String namespace,
      @ApiPathParam(description = "id of identifier") @PathVariable("id") String id)
      throws IdentifiableServiceException {
    return corporateBodyService.getByIdentifier(namespace, id);
  }

  @ApiMethod(description = "Get corporate body by refId")
  @GetMapping(
      value = {"/latest/corporatebodies/{refId:[0-9]+}", "/v3/corporatebodies/{refId:[0-9]+}"},
      produces = "application/json")
  @ApiResponseObject
  public CorporateBody getByRefId(
      @ApiPathParam(description = "reference id") @PathVariable("refId") long refId)
      throws IdentifiableServiceException {
    return corporateBodyService.getByRefId(refId);
  }

  @ApiMethod(description = "Get languages of all corporatebodies")
  @GetMapping(
      value = {"/latest/corporatebodies/languages", "/v3/corporatebodies/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Locale> getLanguages() {
    return corporateBodyService.getLanguages();
  }

  @ApiMethod(description = "Save a newly created corporate body")
  @PostMapping(
      value = {"/latest/corporatebodies", "/v2/corporatebodies"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public CorporateBody save(@RequestBody CorporateBody corporateBody, BindingResult errors)
      throws IdentifiableServiceException {
    return corporateBodyService.save(corporateBody);
  }

  @ApiMethod(description = "Update a corporate body")
  @PutMapping(
      value = {"/latest/corporatebodies/{uuid}", "/v2/corporatebodies/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public CorporateBody update(
      @ApiPathParam(
              description =
                  "UUID of the corporate body, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestBody CorporateBody corporateBody,
      BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, corporateBody.getUuid());
    return corporateBodyService.update(corporateBody);
  }
}
