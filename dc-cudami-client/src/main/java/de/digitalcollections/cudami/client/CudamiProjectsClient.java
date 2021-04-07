package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiProjectsClient extends CudamiBaseClient<ProjectImpl> {

  public CudamiProjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, ProjectImpl.class, mapper);
  }

  public boolean addDigitalObject(UUID projectUuid, UUID digitalObjectUuid) throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/v3/projects/%s/digitalobjects/%s", projectUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/v3/projects/%s/digitalobjects", projectUuid), digitalObjects));
  }

  public Project create() {
    return new ProjectImpl();
  }

  public long count() throws HttpException {
    // No GET endpoint for /latest/projects/count available!
    throw new HttpException("/latest/projects/count", 404);
  }

  public PageResponse<ProjectImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/projects", pageRequest);
  }

  public Project findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/projects/%s", uuid));
  }

  public Project findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/projects/%s?locale=%s", uuid, locale));
  }

  public Project findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v3/projects/identifier/%s:%s.json", namespace, id));
  }

  public PageResponse<DigitalObject> getDigitalObjects(UUID projectUuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v3/projects/%s/digitalobjects", projectUuid),
        pageRequest,
        DigitalObjectImpl.class);
  }

  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format("/v3/projects/%s/digitalobjects/%s", projectUuid, digitalObjectUuid)));
  }

  public Project save(Project project) throws HttpException {
    return doPostRequestForObject("/v2/projects", (ProjectImpl) project);
  }

  public boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format("/v3/projects/%s/digitalobjects", projectUuid),
                digitalObjects,
                String.class));
  }

  public Project update(UUID uuid, Project project) throws HttpException {
    return doPutRequestForObject(String.format("/v2/projects/%s", uuid), (ProjectImpl) project);
  }

  public void delete(UUID uuid) throws HttpException {
    doDeleteRequestForString(String.format("/v3/projects/%s", uuid));
  }

  public List<ProjectImpl> getAll() throws HttpException {
    return doGetRequestForObjectList(String.format("/v2/projectlist", ProjectImpl.class));
  }
}
