package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.agent.GivenName;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.agent.GivenNameImpl;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiGivenNamesClient extends CudamiBaseClient<GivenNameImpl> {

  public CudamiGivenNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, GivenNameImpl.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/givennames/count"));
  }

  public GivenName create() {
    return new GivenNameImpl();
  }

  public PageResponse<GivenNameImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/givennames", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/latest/givennames", pageRequest, language, initial);
  }

  public PageResponse<GivenNameImpl> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/latest/givennames",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public GivenName findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/givennames/%s", uuid));
  }

  public GivenName findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/givennames/identifier?namespace=%s&id=%s", namespace, id));
  }

  public GivenName save(GivenName givenName) throws HttpException {
    return doPostRequestForObject("/latest/givennames", (GivenNameImpl) givenName);
  }

  public GivenName update(UUID uuid, GivenName givenName) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/givennames/%s", uuid), (GivenNameImpl) givenName);
  }
}
