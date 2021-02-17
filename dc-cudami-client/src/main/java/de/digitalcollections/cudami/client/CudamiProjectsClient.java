package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiProjectsClient extends CudamiBaseClient<Project> {

  public CudamiProjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Project.class, mapper);
  }

  public boolean addDigitalObject(UUID projectUuid, UUID digitalObjectUuid) throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                "/latest/projects/%s/digitalobjects/%s", projectUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/latest/projects/%s/digitalobjects", projectUuid), digitalObjects));
  }

  public Project create() {
    return new Project();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/projects/count"));
  }

  public PageResponse<Project> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/projects", pageRequest);
  }

  public Project findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/projects/%s", uuid));
  }

  public Project findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/projects/%s?locale=%s", uuid, locale));
  }

  public Project findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/projects/identifier/%s:%s.json", namespace, id));
  }

  public PageResponse<DigitalObject> getDigitalObjects(UUID projectUuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(String.format("/latest/projects/%s/digitalobjects", projectUuid),
        pageRequest,
        DigitalObject.class);
  }

  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "/latest/projects/%s/digitalobjects/%s", projectUuid, digitalObjectUuid)));
  }

  public Project save(Project project) throws HttpException {
    return doPostRequestForObject("/latest/projects", (Project) project);
  }

  public boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format("/latest/projects/%s/digitalobjects", projectUuid),
                digitalObjects,
                String.class));
  }

  public Project update(UUID uuid, Project project) throws HttpException {
    return doPutRequestForObject(String.format("/latest/projects/%s", uuid), (Project) project);
  }

  public void delete(UUID uuid) throws HttpException {
    doDeleteRequestForString(String.format("/latest/projects/%s", uuid));
  }

  public List<Project> getAll() throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/projectlist", Project.class));
  }
}
