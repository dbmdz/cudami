package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.AgentService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Agent controller")
public class AgentController extends AbstractEntityController<Agent> {

  private final AgentService<Agent> service;

  public AgentController(@Qualifier("agentService") AgentService<Agent> agentService) {
    this.service = agentService;
  }

  @Operation(summary = "Get count of agents")
  @GetMapping(
      value = {"/v6/agents/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return super.count();
  }

  @Operation(summary = "Get all agents as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/agents"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Agent> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Override
  @Operation(
      summary = "Get an agent by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/agents/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Agent> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    Pair<String, String> namespaceAndId =
        ParameterHelper.extractPairOfStringsFromUri(request.getRequestURI(), "^.*?/identifier/");
    if (namespaceAndId.getLeft().isBlank()
        || (namespaceAndId.getRight() == null || namespaceAndId.getRight().isBlank())) {
      throw new ValidationException(
          "No namespace and/or id were provided in a colon separated manner");
    }
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get agent by reference id")
  @GetMapping(
      value = {"/v6/agents/{refId:[0-9]+}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Agent> getByRefId(@PathVariable long refId) throws ServiceException {
    return super.getByRefId(refId);
  }

  @Operation(summary = "Get agent by uuid")
  @GetMapping(
      value = {"/v6/agents/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Agent> getByUuid(@PathVariable UUID uuid) throws ServiceException {
    return super.getByUuid(uuid);
  }

  @Override
  protected AgentService<Agent> getService() {
    return service;
  }
}
