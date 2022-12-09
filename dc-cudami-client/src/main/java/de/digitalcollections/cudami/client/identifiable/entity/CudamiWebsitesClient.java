package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiWebsitesClient extends CudamiEntitiesClient<Website> {

  public CudamiWebsitesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Website.class, mapper, API_VERSION_PREFIX + "/websites");
  }

  public PageResponse<Webpage> findRootWebpages(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/rootpages", baseEndpoint, uuid), pageRequest, Webpage.class);
  }

  public boolean updateRootWebpagesOrder(UUID websiteUuid, List<Webpage> rootpages)
      throws TechnicalException {
    try {
      doPutRequestForString(String.format("%s/%s/rootpages", baseEndpoint, websiteUuid), rootpages);
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }
}
