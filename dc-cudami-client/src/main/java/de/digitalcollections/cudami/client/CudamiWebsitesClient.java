package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiWebsitesClient extends CudamiBaseClient<WebsiteImpl> {

  public CudamiWebsitesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, WebsiteImpl.class, mapper);
  }

  public Website create() {
    return new WebsiteImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v2/websites/count"));
  }

  public PageResponse<WebsiteImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/websites", pageRequest);
  }

  public Website findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/websites/%s", uuid));
  }

  public Website findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/websites/%s?locale=%s", uuid, locale));
  }

  public Website findOneByIdentifier(String namespace, String id) throws HttpException {
    // No GET endpoint for /latest/websites/identifier/%s:%s.json available!
    throw new HttpException(
        String.format("/latest/websites/identifier/%s:%s.json", namespace, id), 404);
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList("/v2/websites/languages", Locale.class);
  }

  public PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v3/websites/%s/rootpages", uuid), pageRequest, WebpageImpl.class);
  }

  public Website save(Website website) throws HttpException {
    return doPostRequestForObject("/v2/websites", (WebsiteImpl) website);
  }

  public Website update(UUID uuid, Website website) throws HttpException {
    return doPutRequestForObject(String.format("/v2/websites/%s", uuid), (WebsiteImpl) website);
  }

  public boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootpages)
      throws HttpException {
    return Boolean.parseBoolean(
        doPutRequestForString(String.format("/v3/websites/%s/rootpages", websiteUuid), rootpages));
  }
}
