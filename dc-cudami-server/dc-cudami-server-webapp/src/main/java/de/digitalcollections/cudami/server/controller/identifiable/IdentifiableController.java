package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import java.util.List;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The identifiable controller", name = "Identifiable controller")
public class IdentifiableController {

  @Autowired
  @Qualifier("identifiableServiceImpl")
  private IdentifiableService service;

  @ApiMethod(description = "Find limited amount of identifiables containing searchTerm in label")
  @GetMapping(
      value = {"/latest/identifiables", "/v2/identifiables"},
      produces = "application/json")
  @ApiResponseObject
  public List<Identifiable> find(
      @RequestParam(name = "searchTerm") String searchTerm,
      @RequestParam(name = "maxResults", required = false, defaultValue = "25") int maxResults) {
    List<Identifiable> identifiables = service.find(searchTerm, maxResults);
    return identifiables;
  }

  @ApiMethod(description = "Get identifiable by uuid")
  @GetMapping(
      value = {"/latest/identifiables/{uuid}", "/v2/identifiables/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public Identifiable findById(@PathVariable UUID uuid) {
    return service.get(uuid);
  }
}
