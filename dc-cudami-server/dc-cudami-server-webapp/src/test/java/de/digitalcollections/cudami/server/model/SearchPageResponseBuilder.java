package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.paging.SearchPageResponse;

public class SearchPageResponseBuilder<T extends Entity>
    extends PageResponseBuilder<T, SearchPageResponse<T>, SearchPageResponseBuilder> {

  public SearchPageResponseBuilder() {
    pageResponse = new SearchPageResponse<>();
  }
}
