package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.agent.FamilyName;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.agent.FamilyNameImpl;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiFamilyNamesClient extends CudamiBaseClient<FamilyNameImpl> {

  public CudamiFamilyNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, FamilyNameImpl.class, mapper);
  }

  public long count() throws HttpException {
    return -1; // URL /latest/familynames/count does not exist!
  }

  public FamilyName create() {
    return new FamilyNameImpl();
  }

  public PageResponse<FamilyNameImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/familynames", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/v2/familynames", pageRequest, language, initial);
  }

  public PageResponse<FamilyNameImpl> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/v2/familynames",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public FamilyName findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/familynames/%s", uuid));
  }

  public FamilyName findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v2/familynames/identifier?namespace=%s&id=%s", namespace, id));
  }

  public FamilyName save(FamilyName familyName) throws HttpException {
    return doPostRequestForObject("/v2/familynames", (FamilyNameImpl) familyName);
  }

  public FamilyName update(UUID uuid, FamilyName familyName) throws HttpException {
    return doPutRequestForObject(
        String.format("/v2/familynames/%s", uuid), (FamilyNameImpl) familyName);
  }
}
