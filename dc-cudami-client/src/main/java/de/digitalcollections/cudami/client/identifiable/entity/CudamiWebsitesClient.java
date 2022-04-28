package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiWebsitesClient extends CudamiEntitiesClient<Website> {

  public CudamiWebsitesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Website.class, mapper, "/v5/websites");
  }

  @Override
  public SearchPageResponse<Website> find(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, searchPageRequest);
  }

  public PageResponse<Webpage> findRootWebpages(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/rootpages", baseEndpoint, uuid), searchPageRequest, Webpage.class);
  }

  public PageResponse<Webpage> findRootWebpages(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/rootpages", baseEndpoint, uuid), pageRequest, Webpage.class);
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/languages", baseEndpoint), Locale.class);
  }

  public boolean updateRootWebpagesOrder(UUID websiteUuid, List<Webpage> rootpages)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPutRequestForString(
            String.format("%s/%s/rootpages", baseEndpoint, websiteUuid), rootpages));
  }
}
