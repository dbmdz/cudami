package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Identifiable controller")
public class IdentifiableController {

  private final IdentifiableService identifiableService;
  private final UrlAliasService urlAliasService;

  public IdentifiableController(
      @Qualifier("identifiableService") IdentifiableService identifiableService,
      UrlAliasService urlAliasService) {
    this.identifiableService = identifiableService;
    this.urlAliasService = urlAliasService;
  }

  @Operation(summary = "Find limited amount of identifiables containing searchTerm in label")
  @GetMapping(
      value = {
        "/v6/identifiables",
        "/v5/identifiables",
        "/v2/identifiables",
        "/latest/identifiables"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Identifiable> find(
      @RequestParam(name = "searchTerm") String searchTerm,
      @RequestParam(name = "maxResults", required = false, defaultValue = "25") int maxResults) {
    List<Identifiable> identifiables = identifiableService.find(searchTerm, maxResults);
    return identifiables;
  }

  @Operation(summary = "Get the LocalizedUrlAliases for an identifiable by its UUID")
  @GetMapping(
      value = {
        "/v6/identifiables/{uuid}/localizedUrlAliases",
        "/v5/identifiables/{uuid}/localizedUrlAliases"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LocalizedUrlAliases> getLocalizedUrlAliases(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws CudamiControllerException {

    try {
      if (getByUuid(uuid) == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      throw new CudamiControllerException(e);
    }

    try {
      return new ResponseEntity<>(urlAliasService.getLocalizedUrlAliases(uuid), HttpStatus.OK);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get an identifiable by namespace and id")
  @GetMapping(
      value = {
        "/v6/identifiables/identifier/{namespace}:{id}",
        "/v6/identifiables/identifier/{namespace}:{id}.json",
        "/v5/identifiables/identifier/{namespace}:{id}",
        "/v5/identifiables/identifier/{namespace}:{id}.json",
        "/v2/identifiables/identifier/{namespace}:{id}",
        "/v2/identifiables/identifier/{namespace}:{id}.json",
        "/latest/identifiables/identifier/{namespace}:{id}",
        "/latest/identifiables/identifier/{namespace}:{id}.json"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Identifiable> getByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {

    Identifiable result = identifiableService.getByIdentifier(namespace, id);
    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get identifiable by uuid")
  @GetMapping(
      value = {
        "/v6/identifiables/{uuid}",
        "/v5/identifiables/{uuid}",
        "/v2/identifiables/{uuid}",
        "/latest/identifiables/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Identifiable getByUuid(@PathVariable UUID uuid) {
    return identifiableService.getByUuid(uuid);
  }
}
