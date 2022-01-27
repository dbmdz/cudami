package de.digitalcollections.cudami.server.controller.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.FamilyNameService;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
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
@Tag(name = "Family name controller")
public class FamilyNameController {

  private final FamilyNameService familyNameService;

  public FamilyNameController(FamilyNameService familyNameservice) {
    this.familyNameService = familyNameservice;
  }

  @Operation(summary = "get all family names")
  @GetMapping(
      value = {"/v5/familynames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FamilyName> findAll(
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

  @Operation(summary = "Get a familyname by namespace and id")
  @GetMapping(
      value = {"/v5/familynames/identifier/{namespace}:{id}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FamilyName> findByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {
    FamilyName result = familyNameService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get a familyname by uuid")
  @GetMapping(
      value = {"/v5/familynames/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FamilyName> get(
      @Parameter(
              example = "",
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

  @Operation(summary = "Get a familyname by namespace and id")
  @GetMapping(
      value = {"/v5/familynames/identifier"},
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

  @Operation(summary = "save a newly created family")
  @PostMapping(
      value = {"/v5/familynames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public FamilyName save(@RequestBody FamilyName familyName, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return familyNameService.save(familyName);
  }

  @Operation(summary = "update a familyname")
  @PutMapping(
      value = {"/v5/familynames/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public FamilyName update(
      @PathVariable("uuid") UUID uuid, @RequestBody FamilyName familyName, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, familyName.getUuid());
    return familyNameService.update(familyName);
  }
}
