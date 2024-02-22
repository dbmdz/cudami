package io.github.dbmdz.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEventsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Event;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.admin.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import io.github.dbmdz.cudami.admin.model.bootstraptable.BTResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Events" endpoints (API). */
@RestController
public class EventsAPIController extends AbstractEntitiesController<Event, CudamiEventsClient> {

  public EventsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forEvents(), client, languageService);
  }

  /*
  Used in templates/events/list.html
  */
  @SuppressFBWarnings
  @GetMapping("/api/events")
  @ResponseBody
  public BTResponse<Event> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        Event.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
  }

  @GetMapping("/api/events/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Event getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }
}
