package de.digitalcollections.cudami.client.identifiable.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiWebpagesClient extends CudamiBaseClient<Webpage> {

  public CudamiWebpagesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Webpage.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/webpages/count"));
  }

  public Webpage create() {
    return new Webpage();
  }

  public PageResponse<Webpage> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/webpages", pageRequest);
  }

  public Webpage findActiveOne(UUID uuid, Locale locale) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/webpages/%s?active=true&pLocale=%s", uuid, locale));
  }

  public PageResponse<Webpage> findActiveSubpages(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/latest/webpages/%s/children?active=true", uuid), searchPageRequest);
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

  public PageResponse<Webpage> findSubpages(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/latest/webpages/%s/children", uuid), searchPageRequest);
  }

  public PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/webpages/%s/children?active=true", uuid),
        pageRequest,
        Webpage.class);
  }

  public List<Webpage> getActiveChildrenTree(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/webpages/%s/childrentree?active=true", uuid));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/latest/webpages/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
  }

  public List<Webpage> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/webpages/%s/children", uuid));
  }

  public PageResponse<Webpage> getChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/webpages/%s/children", uuid), pageRequest);
  }

  public List<Webpage> getChildrenTree(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/webpages/%s/childrentree", uuid));
  }

  public Webpage getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/webpages/%s/parent", uuid));
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid), FileResource.class);
  }

  public Website getWebsite(UUID rootWebpageUuid) throws HttpException {
    return (Website)
        doGetRequestForObject(
            String.format("/latest/webpages/%s/website", rootWebpageUuid), Website.class);
  }

  public Webpage save(Webpage webpage) throws HttpException {
    return doPostRequestForObject("/latest/webpages", webpage);
  }

  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/webpages/%s/webpage", parentWebpageUuid), webpage);
  }

  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/websites/%s/webpage", parentWebsiteUuid), webpage);
  }

  public Webpage update(UUID uuid, Webpage webpage) throws HttpException {
    return doPutRequestForObject(String.format("/latest/webpages/%s", uuid), webpage);
  }

  public boolean updateChildrenOrder(UUID webpageUuid, List<Webpage> children)
      throws HttpException {
    return Boolean.parseBoolean(
        doPutRequestForString(
            String.format("/latest/webpages/%s/children", webpageUuid), children));
  }
}
