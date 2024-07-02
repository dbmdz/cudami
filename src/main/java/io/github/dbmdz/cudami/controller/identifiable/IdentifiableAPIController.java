package io.github.dbmdz.cudami.controller.identifiable;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.model.bootstraptable.BTRequest;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentifiableAPIController extends AbstractUniqueObjectController<Identifiable> {

  public IdentifiableAPIController(CudamiClient client) {
    super(client.forIdentifiables(), null);
  }

  @SuppressFBWarnings
  @GetMapping("/api/identifiables")
  @ResponseBody
  public BTResponse<Identifiable> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = true) String searchTerm)
      throws TechnicalException, ServiceException {
    if (searchTerm != null) {
      // FIXME: This seems to be a thymeleaf problem in templates/identifiables/list.html
      searchTerm = searchTerm.replace("&quot;", "\"");
    }

    BTRequest btRequest = new BTRequest(offset, limit);

    // add sorting in a very limited way, since we have very few fields, which exist
    // in all identifiables
    String sortProperty = "lastModified";
    String sortOrder = "desc";
    Sorting sorting = createSorting(Identifiable.class, sortProperty, sortOrder, null);
    btRequest.setSorting(sorting);

    btRequest.setFiltering(getLabelFiltering(searchTerm));
    PageResponse<Identifiable> pageResponse = service.find(btRequest);
    return new BTResponse<>(pageResponse);
  }

  private Filtering getLabelFiltering(String searchTerm) {
    return Filtering.builder()
        .add(FilterCriterion.builder().withExpression("label").contains(searchTerm).build())
        .build();
  }
}
