package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.ArrayList;

public class AbstractEntitiesController<E extends Entity, C extends CudamiEntitiesClient<E>>
    extends AbstractIdentifiablesController<E, C> {

  protected AbstractEntitiesController(
      C service, LanguageSortingHelper languageSortingHelper, CudamiLocalesClient localeService) {
    super(service, languageSortingHelper, localeService);
  }

  protected PageResponse search(String searchField, String searchTerm, PageRequest pageRequest)
      throws TechnicalException {
    PageResponse<E> pageResponse = super.search(searchField, searchTerm, pageRequest);
    if (pageResponse != null) {
      return pageResponse;
    }

    E entity;
    switch (searchField) {
      case "refId":
        entity = service.getByRefId(Long.parseLong(searchTerm));
        if (entity == null) {
          pageResponse = PageResponse.builder().withContent(new ArrayList()).build();
        } else {
          pageResponse = PageResponse.builder().withContent(entity).build();
        }
        pageResponse.setRequest(pageRequest);
        return pageResponse;
      default:
        return null;
    }
  }
}
