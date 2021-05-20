package de.digitalcollections.cudami.server.controller.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.FamilyNameService;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
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
@Tag(description = "The FamilyName controller", name = "FamilyName controller")
public class FamilyNameController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNameController.class);

  private final FamilyNameService familyNameService;

  public FamilyNameController(FamilyNameService familyNameservice) {
    this.familyNameService = familyNameservice;
  }

  @Operation(summary = "get all family names")
  @GetMapping(
      value = {"/latest/familynames", "/v2/familynames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FamilyName> findAll(
      Pageable pageable,
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
      return familyNameService.find(pageRequest);
    }
    return familyNameService.findByLanguageAndInitial(pageRequest, language, initial);
  }

  @Operation(
      summary =
          "get a familyname as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/familynames/{uuid}", "/v2/familynames/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<FamilyName> get(
      @Parameter(
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              description =
                  "UUID of the familyname, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              example = "",
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {
    FamilyName result;
    if (pLocale == null) {
      result = familyNameService.get(uuid);
    } else {
      result = familyNameService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(
      summary =
          "get a familyname as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/familynames/identifier", "/v2/familynames/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<FamilyName> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    FamilyName result = familyNameService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "save a newly created family")
  @PostMapping(
      value = {"/latest/familynames", "/v2/familynames"},
      produces = "application/json")
  public FamilyName save(@RequestBody FamilyName familyName, BindingResult errors)
      throws IdentifiableServiceException {
    return familyNameService.save(familyName);
  }

  @Operation(summary = "update a familyname")
  @PutMapping(
      value = {"/latest/familynames/{uuid}", "/v2/familynames/{uuid}"},
      produces = "application/json")
  public FamilyName update(
      @PathVariable("uuid") UUID uuid, @RequestBody FamilyName familyName, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, familyName.getUuid());
    return familyNameService.update(familyName);
  }
}
