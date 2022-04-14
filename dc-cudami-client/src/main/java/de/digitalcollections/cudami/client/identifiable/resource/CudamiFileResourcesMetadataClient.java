package de.digitalcollections.cudami.client.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;

/**
 * TODO: implement clients for all different fileresource types (application, audio, image, ....)
 */
public class CudamiFileResourcesMetadataClient extends CudamiIdentifiablesClient<FileResource> {

  public CudamiFileResourcesMetadataClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FileResource.class, mapper, "/v5/fileresources");
  }

  @Override
  public SearchPageResponse<FileResource> find(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, searchPageRequest);
  }

  public SearchPageResponse<FileResource> findByType(
      SearchPageRequest searchPageRequest, String type) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/type/%s", baseEndpoint, type), searchPageRequest);
  }

  public List<Locale> findLanguages() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }
}
