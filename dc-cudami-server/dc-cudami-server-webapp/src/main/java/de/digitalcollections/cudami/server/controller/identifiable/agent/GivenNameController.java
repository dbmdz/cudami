package de.digitalcollections.cudami.server.controller.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.GivenNameService;
import de.digitalcollections.model.identifiable.agent.GivenName;
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
@Tag(name = "Given name controller")
public class GivenNameController {

  private final GivenNameService givenNameService;

  public GivenNameController(GivenNameService givenNameService) {
    this.givenNameService = givenNameService;
  }

  @Operation(summary = "get all given names")
  @GetMapping(
      value = {"/v5/givennames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<GivenName> findAll(
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
      return givenNameService.find(pageRequest);
    }
    return givenNameService.findByLanguageAndInitial(pageRequest, language, initial);
  }

  @Operation(summary = "Get a givenname by namespace and id")
  @GetMapping(
      value = {
        "/v5/givennames/identifier/{namespace}:{id}",
        "/v5/givennames/identifier/{namespace}:{id}.json"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GivenName> getByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {
    GivenName result = givenNameService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get a givenname by namespace and id")
  @GetMapping(
      value = {"/v5/givennames/identifier"},
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

  @Operation(summary = "Get a givenname by uuid")
  @GetMapping(
      value = {"/v5/givennames/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GivenName> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the givenname, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    GivenName result;
    if (pLocale == null) {
      result = givenNameService.getByUuid(uuid);
    } else {
      result = givenNameService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "save a newly created givenname")
  @PostMapping(
      value = {"/v5/givennames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public GivenName save(@RequestBody GivenName givenName, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return givenNameService.save(givenName);
  }

  @Operation(summary = "update a givenname")
  @PutMapping(
      value = {"/v5/givennames/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public GivenName update(
      @PathVariable("uuid") UUID uuid, @RequestBody GivenName givenName, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, givenName.getUuid());
    return givenNameService.update(givenName);
  }
}
