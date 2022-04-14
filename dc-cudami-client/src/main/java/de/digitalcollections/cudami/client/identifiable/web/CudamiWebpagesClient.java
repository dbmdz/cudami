package de.digitalcollections.cudami.client.identifiable.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
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

public class CudamiWebpagesClient extends CudamiIdentifiablesClient<Webpage> {

  public CudamiWebpagesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Webpage.class, mapper, "/v5/webpages");
  }

  public Webpage findActiveOne(UUID uuid, Locale locale) throws TechnicalException {
    return doGetRequestForObject(
        String.format("%s/%s?active=true&pLocale=%s", baseEndpoint, uuid, locale));
  }

  public PageResponse<Webpage> findActiveSubpages(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/children?active=true", baseEndpoint, uuid), searchPageRequest);
  }

  public PageResponse<Webpage> findSubpages(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), searchPageRequest);
  }

  public PageResponse<Webpage> findActiveChildren(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children?active=true", baseEndpoint, uuid),
        pageRequest,
        Webpage.class);
  }

  public List<Webpage> findActiveChildrenTree(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/childrentree?active=true", baseEndpoint, uuid));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws TechnicalException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("%s/%s/breadcrumb", baseEndpoint, uuid), BreadcrumbNavigation.class);
  }

  public List<Webpage> findChildren(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/children", baseEndpoint, uuid));
  }

  public PageResponse<Webpage> findChildren(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), pageRequest);
  }

  public List<Webpage> findChildrenTree(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/childrentree", baseEndpoint, uuid));
  }

  public Webpage getParent(UUID uuid) throws TechnicalException {
    return doGetRequestForObject(String.format("%s/%s/parent", baseEndpoint, uuid));
  }

  public List<FileResource> findRelatedFileResources(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/related/fileresources", baseEndpoint, uuid), FileResource.class);
  }

  public Website getWebsite(UUID rootWebpageUuid) throws TechnicalException {
    return (Website)
        doGetRequestForObject(
            String.format("%s/%s/website", baseEndpoint, rootWebpageUuid), Website.class);
  }

  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid)
      throws TechnicalException {
    return doPostRequestForObject(
        String.format("%s/%s/webpage", baseEndpoint, parentWebpageUuid), webpage);
  }

  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws TechnicalException {
    return doPostRequestForObject(
        String.format("/v5/websites/%s/webpage", parentWebsiteUuid), webpage);
  }

  public boolean updateChildrenOrder(UUID webpageUuid, List<Webpage> children)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPutRequestForString(
            String.format("%s/%s/children", baseEndpoint, webpageUuid), children));
  }
}
