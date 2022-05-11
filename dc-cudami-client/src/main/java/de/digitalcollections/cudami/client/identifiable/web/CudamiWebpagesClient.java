package de.digitalcollections.cudami.client.identifiable.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiWebpagesClient extends CudamiIdentifiablesClient<Webpage> {

  public CudamiWebpagesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Webpage.class, mapper, API_VERSION_PREFIX + "/webpages");
  }

  public PageResponse<Webpage> findActiveChildren(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children?active=true", baseEndpoint, uuid),
        pageRequest,
        Webpage.class);
  }

  public PageResponse<Webpage> findActiveSubpages(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children?active=true", baseEndpoint, uuid), pageRequest);
  }

  public PageResponse<Webpage> findChildren(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), pageRequest);
  }

  public PageResponse<Webpage> findSubpages(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), pageRequest);
  }

  public Webpage getActiveByUuid(UUID uuid, Locale locale) throws TechnicalException {
    return doGetRequestForObject(
        String.format("%s/%s?active=true&pLocale=%s", baseEndpoint, uuid, locale));
  }

  public List<Webpage> getActiveChildrenTree(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/childrentree?active=true", baseEndpoint, uuid));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws TechnicalException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("%s/%s/breadcrumb", baseEndpoint, uuid), BreadcrumbNavigation.class);
  }

  public List<Webpage> getChildren(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/children", baseEndpoint, uuid));
  }

  public List<Webpage> getChildrenTree(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/childrentree", baseEndpoint, uuid));
  }

  public Webpage getParent(UUID uuid) throws TechnicalException {
    return doGetRequestForObject(String.format("%s/%s/parent", baseEndpoint, uuid));
  }

  public List<FileResource> getRelatedFileResources(UUID uuid) throws TechnicalException {
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
        String.format(API_VERSION_PREFIX + "/websites/%s/webpage", parentWebsiteUuid), webpage);
  }

  public boolean updateChildrenOrder(UUID webpageUuid, List<Webpage> children)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPutRequestForString(
            String.format("%s/%s/children", baseEndpoint, webpageUuid), children));
  }
}
