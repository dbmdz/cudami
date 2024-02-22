package io.github.dbmdz.cudami.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiAgentsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.identifiable.entity.AbstractEntitiesController;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Agents" endpoints (API). */
@RestController
public class AgentsAPIController extends AbstractEntitiesController<Agent, CudamiAgentsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentsAPIController.class);

  public AgentsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forAgents(), client, languageService);
  }

  /*
   * Used in templates/agents/view.html as param for
   * templates/fragments/modals/select-entities.html
   */
  @GetMapping("/api/agents/search")
  @ResponseBody
  public PageResponse<Agent> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "searchField", required = false) String searchField,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    // TODO ?: add datalanguage as request param to allow search / autocompletion in
    // selected data language
    String dataLanguage = null;
    PageRequest pageRequest =
        createPageRequest(
            Agent.class, pageNumber, pageSize, sortBy, searchField, searchTerm, dataLanguage);
    PageResponse<Agent> pageResponse = search(searchField, searchTerm, pageRequest);
    if (pageResponse == null) {
      throw new InvalidEndpointRequestException("invalid request param", searchField);
    }
    return pageResponse;
  }
}
