package de.digitalcollections.cudami.client.semantic;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.semantic.Headword;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiHeadwordsClient extends CudamiRestClient<Headword> {

  public CudamiHeadwordsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Headword.class, mapper, "/v5/headwords");
  }

  public SearchPageResponse<Headword> find(SearchPageRequest pageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public List getRelatedArticles(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/related/articles", baseEndpoint, uuid), Article.class);
  }
}
