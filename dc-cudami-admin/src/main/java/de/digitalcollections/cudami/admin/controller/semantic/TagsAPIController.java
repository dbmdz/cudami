package de.digitalcollections.cudami.admin.controller.semantic;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.semantic.CudamiTagsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
public class TagsAPIController extends AbstractPagingAndSortingController<Tag> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TagsAPIController.class);
  private final CudamiTagsClient service;

  public TagsAPIController(CudamiClient client) {
    this.service = client.forTags();
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
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "value") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order)
      throws TechnicalException, ServiceException {
    PageRequest pageRequest = createPageRequest(offset, limit, sort, order);
    if (searchTerm != null) {
      Filtering filtering =
          Filtering.builder()
              .add(FilterCriterion.builder().withExpression("value").contains(searchTerm).build())
              .build();
      pageRequest.setFiltering(filtering);
    }
    PageResponse<Tag> pageResponse = service.find(pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/tags/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Tag getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }
}
