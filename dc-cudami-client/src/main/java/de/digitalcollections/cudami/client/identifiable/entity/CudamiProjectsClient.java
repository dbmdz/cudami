package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiProjectsClient extends CudamiEntitiesClient<Project> {

  public CudamiProjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Project.class, mapper, "/v5/projects");
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

  @Override
  public SearchPageResponse<Project> find(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint, searchPageRequest);
  }

  public List<Project> getAll() throws TechnicalException {
    // TODO: why not using findAll() method of CudamiRestClient? (endpoint must be renamed)
    return doGetRequestForObjectList(String.format("/v5/projectlist", Project.class));
  }

  public SearchPageResponse<DigitalObject> getDigitalObjects(
      UUID projectUuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/digitalobjects", baseEndpoint, projectUuid),
        searchPageRequest,
        DigitalObject.class);
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return doGetRequestForObjectList("/v5/projects/languages", Locale.class);
  }

  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "%s/%s/digitalobjects/%s", baseEndpoint, projectUuid, digitalObjectUuid)));
  }

  public boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format("%s/%s/digitalobjects", baseEndpoint, projectUuid),
                digitalObjects,
                String.class));
  }
}
