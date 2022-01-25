package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;

public class CudamiFamilyNamesClient extends CudamiIdentifiablesClient<FamilyName> {

  public CudamiFamilyNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FamilyName.class, mapper, "/v5/familynames");
  }

  @Override
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
}
