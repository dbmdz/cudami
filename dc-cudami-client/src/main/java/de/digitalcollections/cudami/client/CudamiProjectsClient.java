package de.digitalcollections.cudami.client;

import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import java.util.UUID;

public class CudamiProjectsClient extends CudamiBaseClient<ProjectImpl> {

  public CudamiProjectsClient(String serverUrl) {
    super(serverUrl, ProjectImpl.class);
  }

  public Project create() {
    return new ProjectImpl();
  }

  public long count() throws Exception {
    return Long.parseLong(doGetRequestForString("/latest/projects/count"));
  }

  public PageResponse<ProjectImpl> find(PageRequest pageRequest) throws Exception {
    return doGetRequestForPagedObjectList("/latest/projects", pageRequest);
  }

  public Project findOne(UUID uuid) throws Exception {
    return doGetRequestForObject(String.format("/latest/projects/%s", uuid));
  }

  public Project findOne(UUID uuid, String locale) throws Exception {
    return doGetRequestForObject(String.format("/latest/projects/%s?locale=%s", uuid, locale));
  }

  public Project findOneByIdentifier(String namespace, String id) throws Exception {
    return doGetRequestForObject(
        String.format("/latest/projects/identifier/%s:%s.json", namespace, id));
  }

  public Project save(Project project) throws Exception {
    return doPostRequestForObject("/latest/projects", (ProjectImpl) project);
  }

  public Project update(UUID uuid, Project project) throws Exception {
    return doPutRequestForObject(String.format("/latest/projects/%s", uuid), (ProjectImpl) project);
  }
}
