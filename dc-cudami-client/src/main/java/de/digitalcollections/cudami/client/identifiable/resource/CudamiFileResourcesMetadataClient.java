package de.digitalcollections.cudami.client.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * TODO: implement clients for all different fileresource types (application, audio, image, ....)
 */
public class CudamiFileResourcesMetadataClient extends CudamiBaseClient<FileResource> {

  public CudamiFileResourcesMetadataClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FileResource.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/fileresources/count"));
  }

  public FileResource create() {
    return new FileResource();
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead */
  public PageResponse<FileResource> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/fileresources", pageRequest);
  }

  public SearchPageResponse<FileResource> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/fileresources/search", searchPageRequest);
  }

  public List<FileResource> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<FileResource> response = find(searchPageRequest);
    return response.getContent();
  }

  public SearchPageResponse<FileResource> findFileResourcesByType(
      SearchPageRequest searchPageRequest, String type) throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/latest/fileresources/type/%s", type), searchPageRequest);
  }

  public FileResource findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/fileresources/%s", uuid));
  }

  public FileResource findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/fileresources/identifier/%s:%s.json", namespace, id));
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList("/latest/fileresources/languages", Locale.class);
  }

  public FileResource save(FileResource fileResource) throws HttpException {
    return doPostRequestForObject("/latest/fileresources", fileResource);
  }

  public FileResource update(UUID uuid, FileResource fileResource) throws HttpException {
    return doPutRequestForObject(String.format("/latest/fileresources/%s", uuid), fileResource);
  }
}
