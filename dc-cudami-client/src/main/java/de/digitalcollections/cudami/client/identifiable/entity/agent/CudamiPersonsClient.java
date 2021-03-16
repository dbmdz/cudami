package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiPersonsClient extends CudamiBaseClient<Person> {

  public CudamiPersonsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Person.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/persons/count"));
  }

  public Person create() {
    return new Person();
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead */
  public PageResponse<Person> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/persons", pageRequest);
  }

  public SearchPageResponse<Person> find(SearchPageRequest pageRequest) throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/persons", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/latest/persons", pageRequest, language, initial);
  }

  public PageResponse<Person> findByLanguageAndInitial(
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

  public PageResponse<Person> findByPlaceOfBirth(PageRequest pageRequest, UUID uuidGeoLocation)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        "/latest/persons/placeofbirth/" + uuidGeoLocation.toString(), pageRequest);
  }

  public PageResponse<Person> findByPlaceOfDeath(PageRequest pageRequest, UUID uuidGeoLocation)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        "/latest/persons/placeofdeath/" + uuidGeoLocation.toString(), pageRequest);
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
        String.format("/latest/persons/%s/digitalobjects", uuidPerson), DigitalObject.class);
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList("/latest/persons/languages", Locale.class);
  }

  public List getWorks(UUID uuidPerson) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/persons/%s/works", uuidPerson), Work.class);
  }

  public Person save(Person person) throws HttpException {
    return doPostRequestForObject("/latest/persons", person);
  }

  public Person update(UUID uuid, Person person) throws HttpException {
    return doPutRequestForObject(String.format("/latest/persons/%s", uuid), person);
  }
}
