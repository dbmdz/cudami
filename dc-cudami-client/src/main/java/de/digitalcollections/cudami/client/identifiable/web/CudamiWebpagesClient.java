package de.digitalcollections.cudami.client.identifiable.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.identifiable.entity.Website;
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

  public Webpage findActiveOne(UUID uuid, Locale locale) throws HttpException {
    return doGetRequestForObject(
        String.format("%s/%s?active=true&pLocale=%s", baseEndpoint, uuid, locale));
  }

  public PageResponse<Webpage> findActiveSubpages(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/children?active=true", baseEndpoint, uuid), searchPageRequest);
  }

  /**
   * Only reason for overwriting is name of param "pLocale" instead "locale": FIXME?
   *
   * @deprecated This method is subject to be removed. Use {@link
   *     CudamiIdentifiablesClient#getByUuidAndLocale(java.util.UUID, java.lang.String)} instead.
   */
  @Override
  @Deprecated(forRemoval = true)
  public Webpage findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v5/webpages/%s?pLocale=%s", uuid, locale));
  }

  public PageResponse<Webpage> findSubpages(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), searchPageRequest);
  }

  public PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children?active=true", baseEndpoint, uuid),
        pageRequest,
        Webpage.class);
  }

  public List<Webpage> getActiveChildrenTree(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("%s/%s/childrentree?active=true", baseEndpoint, uuid));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("%s/%s/breadcrumb", baseEndpoint, uuid), BreadcrumbNavigation.class);
  }

  public List<Webpage> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("%s/%s/children", baseEndpoint, uuid));
  }

  public PageResponse<Webpage> getChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), pageRequest);
  }

  public List<Webpage> getChildrenTree(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("%s/%s/childrentree", baseEndpoint, uuid));
  }

  public Webpage getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("%s/%s/parent", baseEndpoint, uuid));
  }

  public Website getWebsite(UUID rootWebpageUuid) throws HttpException {
    return (Website)
        doGetRequestForObject(
            String.format("%s/%s/website", baseEndpoint, rootWebpageUuid), Website.class);
  }

  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("%s/%s/webpage", baseEndpoint, parentWebpageUuid), webpage);
  }

  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/v5/websites/%s/webpage", parentWebsiteUuid), webpage);
  }

  public boolean updateChildrenOrder(UUID webpageUuid, List<Webpage> children)
      throws HttpException {
    return Boolean.parseBoolean(
        doPutRequestForString(
            String.format("%s/%s/children", baseEndpoint, webpageUuid), children));
  }
}
