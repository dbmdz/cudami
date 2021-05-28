package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
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

public class CudamiWebsitesClient extends CudamiBaseClient<Website> {

  public CudamiWebsitesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Website.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/websites/count"));
  }

  public Website create() {
    return new Website();
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead */
  public PageResponse<Website> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/websites", pageRequest);
  }

  public SearchPageResponse<Website> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/websites", searchPageRequest);
  }

  public Website findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/websites/%s", uuid));
  }

  public Website findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v5/websites/%s?locale=%s", uuid, locale));
  }

  public Website findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/websites/identifier/%s:%s.json", namespace, id));
  }

  public PageResponse<Webpage> findRootPages(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/v5/websites/%s/rootpages", uuid), searchPageRequest, Webpage.class);
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList("/v5/websites/languages", Locale.class);
  }

  public PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/websites/%s/rootpages", uuid), pageRequest, Webpage.class);
  }

  public Website save(Website website) throws HttpException {
    return doPostRequestForObject("/v5/websites", website);
  }

  public Website update(UUID uuid, Website website) throws HttpException {
    return doPutRequestForObject(String.format("/v5/websites/%s", uuid), website);
  }

  public boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootpages)
      throws HttpException {
    return Boolean.parseBoolean(
        doPutRequestForString(String.format("/v5/websites/%s/rootpages", websiteUuid), rootpages));
  }
}
