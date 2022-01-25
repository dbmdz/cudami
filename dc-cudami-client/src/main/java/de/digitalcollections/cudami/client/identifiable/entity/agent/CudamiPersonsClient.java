package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiPersonsClient extends CudamiIdentifiablesClient<Person> {

  public CudamiPersonsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Person.class, mapper, "/v5/persons");
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #find(SearchPageRequest)} instead */
  @Override
  public PageResponse<Person> find(PageRequest pageRequest) throws HttpException {
    return super.find(pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial(baseEndpoint, pageRequest, language, initial);
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
        baseEndpoint,
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
        baseEndpoint + "/placeofbirth/" + uuidGeoLocation.toString(), pageRequest);
  }

  public PageResponse<Person> findByPlaceOfDeath(PageRequest pageRequest, UUID uuidGeoLocation)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        baseEndpoint + "/placeofdeath/" + uuidGeoLocation.toString(), pageRequest);
  }

  public List getDigitalObjects(UUID uuidPerson) throws HttpException {
    return doGetRequestForObjectList(
        String.format(baseEndpoint + "/%s/digitalobjects", uuidPerson), DigitalObject.class);
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }

  public List getWorks(UUID uuidPerson) throws HttpException {
    return doGetRequestForObjectList(
        String.format(baseEndpoint + "/%s/works", uuidPerson), Work.class);
  }
}
