package de.digitalcollections.cudami.client;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import java.util.List;
import java.util.UUID;

public class CudamiWebsitesClient extends CudamiBaseClient<WebsiteImpl> {

  public CudamiWebsitesClient(String serverUrl) {
    super(serverUrl, WebsiteImpl.class);
  }

  public Website create() {
    return new WebsiteImpl();
  }

  public long count() throws Exception {
    return Long.parseLong(doGetRequestForString("/latest/websites/count"));
  }

  public PageResponse<WebsiteImpl> find(PageRequest pageRequest) throws Exception {
    return doGetRequestForPagedObjectList("/latest/websites", pageRequest);
  }

  public Website findOne(UUID uuid) throws Exception {
    return doGetRequestForObject(String.format("/latest/websites/%s", uuid));
  }

  public Website findOne(UUID uuid, String locale) throws Exception {
    return doGetRequestForObject(String.format("/latest/websites/%s?locale=%s", uuid, locale));
  }

  public Website findOneByIdentifier(String namespace, String id) throws Exception {
    return doGetRequestForObject(
        String.format("/latest/websites/identifier/%s:%s.json", namespace, id));
  }

  public List<Webpage> getRootPages(UUID uuid) throws Exception {
    return doGetRequestForObjectList(
        String.format("/latest/websites/%s/rootPages", uuid), WebpageImpl.class);
  }

  public Website save(Website website) throws Exception {
    return doPostRequestForObject("/latest/websites", (WebsiteImpl) website);
  }

  public Website update(UUID uuid, Website website) throws Exception {
    return doPutRequestForObject(String.format("/latest/websites/%s", uuid), (WebsiteImpl) website);
  }
}
