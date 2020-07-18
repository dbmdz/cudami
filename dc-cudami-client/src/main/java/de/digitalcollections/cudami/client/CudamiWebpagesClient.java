package de.digitalcollections.cudami.client;

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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiWebpagesClient extends CudamiBaseClient<WebpageImpl> {

  public CudamiWebpagesClient(String serverUrl) {
    super(serverUrl, WebpageImpl.class);
  }

  public Webpage create() {
    return new WebpageImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/webpages/count"));
  }

  public PageResponse<WebpageImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/webpages", pageRequest);
  }

  public Webpage findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/webpages/%s", uuid));
  }

  public Webpage findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Webpage findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/webpages/%s?pLocale=%s", uuid, locale));
  }

  public Webpage findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/webpages/identifier/%s:%s.json", namespace, id));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/latest/webpages/%s/breadcrumb", uuid), BreadcrumbNavigationImpl.class);
  }

  public List<WebpageImpl> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/webpages/%s/children", uuid));
  }

  public PageResponse<WebpageImpl> getChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/webpages/%s/children", uuid), pageRequest);
  }

  public Webpage getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/webpages/%s/parent", uuid));
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public Website getWebsite(UUID rootWebpageUuid) throws HttpException {
    return (Website)
        doGetRequestForObject(
            String.format("/latest/webpages/%s/website", rootWebpageUuid), WebsiteImpl.class);
  }

  public Webpage save(Webpage webpage) throws HttpException {
    return doPostRequestForObject("/latest/webpages", (WebpageImpl) webpage);
  }

  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/websites/%s/webpage", parentWebsiteUuid), (WebpageImpl) webpage);
  }

  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/webpages/%s/webpage", parentWebpageUuid), (WebpageImpl) webpage);
  }

  public Webpage update(UUID uuid, Webpage webpage) throws HttpException {
    return doPutRequestForObject(String.format("/latest/webpages/%s", uuid), (WebpageImpl) webpage);
  }
}
