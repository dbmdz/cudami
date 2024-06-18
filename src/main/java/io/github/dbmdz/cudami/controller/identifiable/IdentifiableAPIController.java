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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentifiableAPIController extends AbstractUniqueObjectController<Identifiable> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeAPIController.class);

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
    BTRequest btRequest = new BTRequest(offset, limit);

    // add sorting in a very limited way, since we have very few fields, which exist
    // in all identifiables
    String sortProperty = "lastModified";
    String sortOrder = "desc";
    Sorting sorting = createSorting(Identifiable.class, sortProperty, sortOrder, null);
    btRequest.setSorting(sorting);

    // add filtering
    Filtering filtering = null;

    // Step1: Query for identifier
    btRequest.setFiltering(getIdentifierFiltering(searchTerm));
    PageResponse<Identifiable> pageResponse = service.find(btRequest);
    if (pageResponse.hasContent()) {
      return new BTResponse<>(pageResponse);
    }

    // Step2 (if 1 did not return anything): Query for label
    btRequest.setFiltering(getLabelFiltering(searchTerm));
    pageResponse = service.find(btRequest);
    return new BTResponse<>(pageResponse);
  }

  private Filtering getIdentifierFiltering(String searchTerm) {
    return Filtering.builder()
        .add(
            FilterCriterion.builder().withExpression("identifiers.id").isEquals(searchTerm).build())
        .build();
  }

  private Filtering getLabelFiltering(String searchTerm) throws TechnicalException {
    String expression = "label";
    String dataLanguage = null;
    if (isMultiLanguageField(Identifiable.class, "label")) {
      // FIXME: Does `dataLanguage` contain the script, e.g. "de-Latn"? What about the DB?
      dataLanguage = getDataLanguage(dataLanguage, languageService);
      // convention: add datalanguage as "sub"-expression to expression - to be handled later on
      // serverside
      expression += "." + dataLanguage;
    }
    return Filtering.builder()
        .add(FilterCriterion.builder().withExpression(expression).contains(searchTerm).build())
        .build();
  }
}
