package de.digitalcollections.cudami.client.alias;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiUrlAliasClient extends CudamiBaseClient<UrlAlias> {

  public CudamiUrlAliasClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, UrlAlias.class, mapper);
  }

  public LocalizedUrlAliases findOneByWebsiteUuidAndSlug(UUID websiteUuid, String slug)
      throws HttpException {
    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format("/v5/urlaliases/%s/%s", websiteUuid, slug), LocalizedUrlAliases.class);
  }

  public UrlAlias save(UrlAlias urlAlias) throws HttpException {
    return doPostRequestForObject("/v5/urlaliases", urlAlias);
  }

  public UrlAlias update(UUID uuid, UrlAlias urlAlias) throws HttpException {
    return doPutRequestForObject(String.format("/v5/urlaliases/%s", uuid), urlAlias);
  }

  public UrlAlias findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/urlaliases/%s", uuid));
  }

  public boolean delete(UUID uuid) throws HttpException {
    return Boolean.parseBoolean(doDeleteRequestForString(String.format("/v5/urlaliases/%s", uuid)));
  }

  public SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        "/v5/urlaliases/search", searchPageRequest, LocalizedUrlAliases.class);
  }

  /* TODO
  public boolean isMainLink(UrlAlias urlAlias, String slug);
  public LocalizedUrlAlias appendUrlAliases(UUID uuid) throws HttpException;
  public LocalizedUrlAlias getLocalizedUrlAliases(UUID uuid) throws HttpException;
   */

}
