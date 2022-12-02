package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiPersonsClient extends CudamiEntitiesClient<Person> {

  public CudamiPersonsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Person.class, mapper, API_VERSION_PREFIX + "/persons");
  }

  public PageResponse<Person> findByGeoLocationOfBirth(
      PageRequest pageRequest, UUID uuidGeoLocation) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/placeofbirth/%s", baseEndpoint, uuidGeoLocation.toString()), pageRequest);
  }

  public PageResponse<Person> findByGeoLocationOfDeath(
      PageRequest pageRequest, UUID uuidGeoLocation) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/placeofdeath/%s", baseEndpoint, uuidGeoLocation.toString()), pageRequest);
  }

  public List<DigitalObject> getDigitalObjects(UUID uuidPerson) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/digitalobjects", baseEndpoint, uuidPerson), DigitalObject.class);
  }

  public List getWorks(UUID uuidPerson) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/works", baseEndpoint, uuidPerson), Work.class);
  }
}
