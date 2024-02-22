package io.github.dbmdz.cudami.controller;

import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.model.bootstraptable.BTRequest;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;

public class AbstractUniqueObjectController<U extends UniqueObject>
    extends AbstractPagingAndSortingController {

  protected final CudamiRestClient<U> service;

  @SuppressFBWarnings
  public AbstractUniqueObjectController(
      CudamiRestClient<U> service, LanguageService languageService) {
    super(languageService);
    this.service = service;
  }

  protected BTResponse<U> find(
      Class targetClass,
      int offset,
      int limit,
      String sortProperty,
      String sortOrder,
      String searchProperty,
      String searchTerm,
      String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            targetClass,
            offset,
            limit,
            sortProperty,
            sortOrder,
            searchProperty,
            searchTerm,
            dataLanguage);
    PageResponse<U> pageResponse = service.find(btRequest);
    return new BTResponse<>(pageResponse);
  }
}
