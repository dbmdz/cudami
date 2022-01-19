package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;

public class CudamiHumanSettlementsClient extends CudamiIdentifiablesClient<HumanSettlement> {

  public CudamiHumanSettlementsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, HumanSettlement.class, mapper, "/v5/humansettlements");
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial(baseEndpoint, pageRequest, language, initial);
  }

  public PageResponse<HumanSettlement> findByLanguageAndInitial(
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

  @Override
  public HumanSettlement findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("%s/identifier?namespace=%s&id=%s", baseEndpoint, namespace, id));
  }
}
