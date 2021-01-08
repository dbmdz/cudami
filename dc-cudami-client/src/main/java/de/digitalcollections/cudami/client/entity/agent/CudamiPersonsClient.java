package de.digitalcollections.cudami.client.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.PersonImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.WorkImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiPersonsClient extends CudamiBaseClient<PersonImpl> {

  public CudamiPersonsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, PersonImpl.class, mapper);
  }

  public Person create() {
    return new PersonImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/persons/count"));
  }

  public PageResponse<PersonImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/persons", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/latest/persons", pageRequest, language, initial);
  }

  public PageResponse<PersonImpl> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/latest/persons",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public PageResponse<PersonImpl> findByLocationOfBirth(
      PageRequest pageRequest, UUID uuidGeoLocation) throws HttpException {
    return doGetRequestForPagedObjectList(
        "/latest/persons/placeOfBirth/" + uuidGeoLocation.toString(), pageRequest);
  }

  public PageResponse<PersonImpl> findByLocationOfDeath(
      PageRequest pageRequest, UUID uuidGeoLocation) throws HttpException {
    return doGetRequestForPagedObjectList(
        "/latest/persons/placeOfDeath/" + uuidGeoLocation.toString(), pageRequest);
  }

  public Person findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/persons/%s", uuid));
  }

  public Person findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/persons/identifier?namespace=%s&id=%s", namespace, id));
  }

  public List getDigitalObjects(UUID uuidPerson) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/persons/%s/digitalobjects", uuidPerson), DigitalObjectImpl.class);
  }

  public List getWorks(UUID uuidPerson) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/persons/%s/works", uuidPerson), WorkImpl.class);
  }

  public Person save(Person person) throws HttpException {
    return doPostRequestForObject("/latest/persons", (PersonImpl) person);
  }

  public Person update(UUID uuid, Person person) throws HttpException {
    return doPutRequestForObject(String.format("/latest/persons/%s", uuid), (PersonImpl) person);
  }
}
