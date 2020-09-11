package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CorporationService;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
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
@Api(description = "The corporation controller", name = "Corporation controller")
public class CorporationController {

  private static final Pattern GNDID_PATTERN = Pattern.compile("\\d+-\\d");

  private CorporationService corporationService;

  @Autowired
  public CorporationController(CorporationService corporationService) {
    this.corporationService = corporationService;
  }

  @ApiMethod(description = "Get all corporations")
  @GetMapping(
      value = {"/latest/corporations", "/v2/corporations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Corporation> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "lastModified")
          String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return corporationService.find(pageRequest);
  }

  // Test-URL: http://localhost:9000/latest/corporations/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get an corporation as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/corporations/{uuid}", "/v2/corporations/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Corporation> get(
      @ApiPathParam(
              description =
                  "UUID of the corporation, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Corporation corporation;
    if (pLocale == null) {
      corporation = corporationService.get(uuid);
    } else {
      corporation = corporationService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(corporation, HttpStatus.OK);
  }

  @ApiMethod(description = "Save a newly created corporation")
  @PostMapping(
      value = {"/latest/corporations", "/v2/corporations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Corporation save(@RequestBody Corporation corporation, BindingResult errors)
      throws IdentifiableServiceException {
    return corporationService.save(corporation);
  }

  @ApiMethod(
      description = "Save a newly created corporation fetched by GND-ID from external system")
  @PostMapping(
      value = {"/latest/corporations/gnd/{gndId}", "/v3/corporations/gnd/{gndId}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Corporation fetchAndSaveByGndId(
      @ApiPathParam(description = "GND-ID of the corporation, e.g. <tt>2007744-0</tt>")
          @PathVariable("gndId")
          String gndId)
      throws IdentifiableServiceException {
    if (!GNDID_PATTERN.matcher(gndId).matches()) {
      throw new IllegalArgumentException("Invalid GND ID: " + gndId);
    }
    return corporationService.fetchAndSaveByGndId(gndId);
  }

  @ApiMethod(description = "Update an corporation")
  @PutMapping(
      value = {"/latest/corporations/{uuid}", "/v2/corporations/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Corporation update(
      @ApiPathParam(
              description =
                  "UUID of the corporation, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestBody Corporation corporation,
      BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, corporation.getUuid());
    return corporationService.update(corporation);
  }
}
