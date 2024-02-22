package io.github.dbmdz.cudami.controller.semantic;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.semantic.Tag;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Tags" endpoints (API). */
@RestController
public class TagsAPIController extends AbstractUniqueObjectController<Tag> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TagsAPIController.class);

  public TagsAPIController(CudamiClient client) {
    // no "languageService" needed / no multilingual fields
    super(client.forTags(), null);
  }

  @GetMapping("/api/tags/new")
  @ResponseBody
  public Tag createModel() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/tags")
  @ResponseBody
  public BTResponse<Tag> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "value") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder)
      throws TechnicalException, ServiceException {
    // no "dataLanguage" / no multilingual fields
    return find(Tag.class, offset, limit, sortProperty, sortOrder, "value", searchTerm, null);
  }

  @GetMapping("/api/tags/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Tag getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }
}
