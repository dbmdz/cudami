package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiWebpagesClient extends CudamiBaseClient<WebpageImpl> {

  public CudamiWebpagesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, WebpageImpl.class, mapper);
  }

  public Webpage create() {
    return new WebpageImpl();
  }

  public long count() throws HttpException {
    // No GET endpoint for /latest/webpages/count available!
    throw new HttpException("/latest/webpages/count", 404);
  }

  public PageResponse<WebpageImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/webpages", pageRequest);
  }

  public Webpage findActiveOne(UUID uuid, Locale locale) throws HttpException {
    return doGetRequestForObject(
        String.format("/v3/webpages/%s?active=true&pLocale=%s", uuid, locale));
  }

  public Webpage findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v3/webpages/%s", uuid));
  }

  public Webpage findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Webpage findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v3/webpages/%s?pLocale=%s", uuid, locale));
  }

  public Webpage findOneByIdentifier(String namespace, String id) throws HttpException {
    // No GET endpoint for /latest/webpages/identifier/%s:%s.json available!
    throw new HttpException(
        String.format("/latest/webpages/identifier/%s:%s.json", namespace, id), 404);
  }

  public PageResponse<WebpageImpl> getActiveChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v3/webpages/%s/children?active=true", uuid),
        pageRequest,
        WebpageImpl.class);
  }

  public List<WebpageImpl> getActiveChildrenTree(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v3/webpages/%s/childrentree?active=true", uuid));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/v3/webpages/%s/breadcrumb", uuid), BreadcrumbNavigationImpl.class);
  }

  public List<WebpageImpl> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v3/webpages/%s/children", uuid));
  }

  public List<WebpageImpl> getChildrenTree(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v3/webpages/%s/childrentree", uuid));
  }

  public PageResponse<WebpageImpl> getChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v3/webpages/%s/children", uuid), pageRequest);
  }

  public Webpage getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v3/webpages/%s/parent", uuid));
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public Website getWebsite(UUID rootWebpageUuid) throws HttpException {
    return (Website)
        doGetRequestForObject(
            String.format("/v3/webpages/%s/website", rootWebpageUuid), WebsiteImpl.class);
  }

  public Webpage save(Webpage webpage) throws HttpException {
    // No POST endpoint for /latest/webpages available!
    throw new HttpException("/latest/webpages", 404);
  }

  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/v2/websites/%s/webpage", parentWebsiteUuid), (WebpageImpl) webpage);
  }

  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/v2/webpages/%s/webpage", parentWebpageUuid), (WebpageImpl) webpage);
  }

  public Webpage update(UUID uuid, Webpage webpage) throws HttpException {
    return doPutRequestForObject(String.format("/v2/webpages/%s", uuid), (WebpageImpl) webpage);
  }

  public boolean updateChildrenOrder(UUID webpageUuid, List<Webpage> children)
      throws HttpException {
    return Boolean.parseBoolean(
        doPutRequestForString(String.format("/v3/webpages/%s/children", webpageUuid), children));
  }
}
