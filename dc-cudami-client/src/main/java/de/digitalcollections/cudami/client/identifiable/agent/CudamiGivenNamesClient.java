package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiGivenNamesClient extends CudamiBaseClient<GivenName> {

  public CudamiGivenNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, GivenName.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/givennames/count"));
  }

  public GivenName create() {
    return new GivenName();
  }

  public PageResponse<GivenName> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/givennames", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/v5/givennames", pageRequest, language, initial);
  }

  public PageResponse<GivenName> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/v5/givennames",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public GivenName findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/givennames/%s", uuid));
  }

  public GivenName findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/givennames/identifier?namespace=%s&id=%s", namespace, id));
  }

  public GivenName save(GivenName givenName) throws HttpException {
    return doPostRequestForObject("/v5/givennames", givenName);
  }

  public GivenName update(UUID uuid, GivenName givenName) throws HttpException {
    return doPutRequestForObject(String.format("/v5/givennames/%s", uuid), givenName);
  }
}
