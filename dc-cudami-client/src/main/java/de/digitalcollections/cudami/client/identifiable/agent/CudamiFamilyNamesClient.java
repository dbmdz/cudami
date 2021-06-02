package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiFamilyNamesClient extends CudamiBaseClient<FamilyName> {

  public CudamiFamilyNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FamilyName.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/familynames/count"));
  }

  public FamilyName create() {
    return new FamilyName();
  }

  public SearchPageResponse<FamilyName> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/familynames", searchPageRequest);
  }

  public SearchPageResponse findByLanguageAndInitial(
      SearchPageRequest searchPageRequest, String language, String initial) throws HttpException {
    return (SearchPageResponse<FamilyName>)
        findByLanguageAndInitial("/v5/familynames", searchPageRequest, language, initial);
  }

  public SearchPageResponse<FamilyName> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return (SearchPageResponse<FamilyName>)
        findByLanguageAndInitial(
            "/v5/familynames",
            pageNumber,
            pageSize,
            sortField,
            sortDirection,
            nullHandling,
            language,
            initial);
  }

  public FamilyName findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/familynames/%s", uuid));
  }

  public FamilyName findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/familynames/identifier?namespace=%s&id=%s", namespace, id));
  }

  public FamilyName save(FamilyName familyName) throws HttpException {
    return doPostRequestForObject("/v5/familynames", familyName);
  }

  public FamilyName update(UUID uuid, FamilyName familyName) throws HttpException {
    return doPutRequestForObject(String.format("/v5/familynames/%s", uuid), familyName);
  }
}
