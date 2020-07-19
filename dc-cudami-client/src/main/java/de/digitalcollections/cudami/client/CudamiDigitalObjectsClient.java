package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.util.List;
import java.util.UUID;

public class CudamiDigitalObjectsClient extends CudamiBaseClient<DigitalObjectImpl> {

  public CudamiDigitalObjectsClient(String serverUrl, ObjectMapper mapper) {
    super(serverUrl, DigitalObjectImpl.class, mapper);
  }

  public DigitalObject create() {
    return new DigitalObjectImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/digitalobjects/count"));
  }

  public PageResponse<DigitalObjectImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/digitalobjects", pageRequest);
  }

  public SearchPageResponse<DigitalObjectImpl> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/digitalobjects/search", searchPageRequest);
  }

  public List<DigitalObjectImpl> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<DigitalObjectImpl> response = find(searchPageRequest);
    return response.getContent();
  }

  public DigitalObject findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/digitalobjects/%s", uuid));
  }

  public DigitalObject findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/digitalobjects/identifier/%s:%s.json", namespace, id));
  }

  public List<FileResource> getFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/digitalobjects/%s/fileresources", uuid), FileResourceImpl.class);
  }

  public List<ImageFileResource> getImageFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/digitalobjects/%s/fileresources/images", uuid),
        ImageFileResourceImpl.class);
  }

  public DigitalObject save(DigitalObject digitalObject) throws HttpException {
    return doPostRequestForObject("/latest/digitalobjects", (DigitalObjectImpl) digitalObject);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/latest/digitalobjects/%s/fileresources", uuid),
        fileResources,
        FileResourceImpl.class);
  }

  public DigitalObject update(UUID uuid, DigitalObject digitalObject) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/digitalobjects/%s", uuid), (DigitalObjectImpl) digitalObject);
  }
}
