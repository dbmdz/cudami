package de.digitalcollections.cudami.client.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;

/**
 * TODO: implement clients for all different fileresource types (application, audio, image, ....)
 */
public class CudamiFileResourcesMetadataClient extends CudamiIdentifiablesClient<FileResource> {

  public CudamiFileResourcesMetadataClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FileResource.class, mapper, API_VERSION_PREFIX + "/fileresources");
  }

  public PageResponse<FileResource> findByType(PageRequest pageRequest, String type)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/type/%s", baseEndpoint, type), pageRequest);
  }
}
