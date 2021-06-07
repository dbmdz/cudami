package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiProjectsClient extends CudamiBaseClient<Project> {

  public CudamiProjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Project.class, mapper);
  }

  public boolean addDigitalObject(UUID projectUuid, UUID digitalObjectUuid) throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/v5/projects/%s/digitalobjects/%s", projectUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/v5/projects/%s/digitalobjects", projectUuid), digitalObjects));
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/projects/count"));
  }

  public Project create() {
    return new Project();
  }

  public void delete(UUID uuid) throws HttpException {
    doDeleteRequestForString(String.format("/v5/projects/%s", uuid));
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead */
  public PageResponse<Project> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/projects", pageRequest);
  }

  public SearchPageResponse<Project> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/projects", searchPageRequest);
  }

  public Project findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/projects/%s", uuid));
  }

  public Project findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v5/projects/%s?locale=%s", uuid, locale));
  }

  public Project findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/projects/identifier/%s:%s.json", namespace, id));
  }

  public List<Project> getAll() throws HttpException {
    return doGetRequestForObjectList(String.format("/v5/projectlist", Project.class));
  }

  public PageResponse<DigitalObject> getDigitalObjects(UUID projectUuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/projects/%s/digitalobjects", projectUuid),
        pageRequest,
        DigitalObject.class);
  }

  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format("/v5/projects/%s/digitalobjects/%s", projectUuid, digitalObjectUuid)));
  }

  public Project save(Project project) throws HttpException {
    return doPostRequestForObject("/v5/projects", project);
  }

  public boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format("/v5/projects/%s/digitalobjects", projectUuid),
                digitalObjects,
                String.class));
  }

  public Project update(UUID uuid, Project project) throws HttpException {
    return doPutRequestForObject(String.format("/v5/projects/%s", uuid), project);
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList("/v5/projects/languages", Locale.class);
  }
}
