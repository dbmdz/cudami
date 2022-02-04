package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.http.HttpException;
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
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, searchPageRequest);
  }

  public PageResponse<Webpage> findRootPages(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/rootpages", baseEndpoint, uuid), searchPageRequest, Webpage.class);
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList(String.format("%s/languages", baseEndpoint), Locale.class);
  }

  public PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/rootpages", baseEndpoint, uuid), pageRequest, Webpage.class);
  }

  public boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootpages)
      throws HttpException {
    return Boolean.parseBoolean(
        doPutRequestForString(
            String.format("%s/%s/rootpages", baseEndpoint, websiteUuid), rootpages));
  }
}
