package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

/**
 * TODO: implement clients for all different fileresource types (application, audio, image, ....)
 */
public class CudamiFileResourcesMetadataClient extends CudamiBaseClient<FileResourceImpl> {

  public CudamiFileResourcesMetadataClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FileResourceImpl.class, mapper);
  }

  public FileResource create() {
    return new FileResourceImpl();
  }

  public long count() throws HttpException {
    // No GET endpoint for /latest/fileresources/count available!
    throw new HttpException("/latest/fileresources/count", 404);
  }

  public PageResponse<FileResourceImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/fileresources", pageRequest);
  }

  public SearchPageResponse<FileResourceImpl> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    // No GET endpoint for /latest/fileresources/search available!
    throw new HttpException("/latest/fileresources/search", 404);
  }

  public List<FileResourceImpl> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<FileResourceImpl> response = find(searchPageRequest);
    return response.getContent();
  }

  public SearchPageResponse<FileResourceImpl> findFileResourcesByType(
      SearchPageRequest searchPageRequest, String type) throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/v2/fileresources/type/%s", type), searchPageRequest);
  }

  public FileResource findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/fileresources/%s", uuid));
  }

  public FileResource findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v2/fileresources/identifier/%s:%s.json", namespace, id));
  }

  public FileResource save(FileResource fileResource) throws HttpException {
    return doPostRequestForObject("/v2/fileresources", (FileResourceImpl) fileResource);
  }

  public FileResource update(UUID uuid, FileResource fileResource) throws HttpException {
    return doPutRequestForObject(
        String.format("/v2/fileresources/%s", uuid), (FileResourceImpl) fileResource);
  }
}
