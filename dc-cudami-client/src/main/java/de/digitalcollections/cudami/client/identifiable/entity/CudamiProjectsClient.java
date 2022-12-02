package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiProjectsClient extends CudamiEntitiesClient<Project> {

  public CudamiProjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Project.class, mapper, API_VERSION_PREFIX + "/projects");
  }

  public boolean addDigitalObject(UUID projectUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                "%s/%s/digitalobjects/%s", baseEndpoint, projectUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("%s/%s/digitalobjects", baseEndpoint, projectUuid), digitalObjects));
  }

  public PageResponse<DigitalObject> findDigitalObjects(UUID projectUuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/digitalobjects", baseEndpoint, projectUuid),
        pageRequest,
        DigitalObject.class);
  }

  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "%s/%s/digitalobjects/%s", baseEndpoint, projectUuid, digitalObjectUuid)));
  }

  public boolean setDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format("%s/%s/digitalobjects", baseEndpoint, projectUuid),
                digitalObjects,
                String.class));
  }
}
