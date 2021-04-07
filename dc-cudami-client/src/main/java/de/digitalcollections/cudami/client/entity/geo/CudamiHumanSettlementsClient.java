package de.digitalcollections.cudami.client.entity.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.geo.HumanSettlement;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.geo.HumanSettlementImpl;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiHumanSettlementsClient extends CudamiBaseClient<HumanSettlementImpl> {

  public CudamiHumanSettlementsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, HumanSettlementImpl.class, mapper);
  }

  public HumanSettlement create() {
    return new HumanSettlementImpl();
  }

  public long count() throws HttpException {
    return -1; // URL /latest/human_settlements/count does not exist
  }

  public PageResponse<HumanSettlementImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/human_settlements", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/v2/human_settlements", pageRequest, language, initial);
  }

  public PageResponse<HumanSettlementImpl> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/v2/human_settlements",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public HumanSettlement findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/human_settlements/%s", uuid));
  }

  public HumanSettlement findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v2/human_settlements/identifier?namespace=%s&id=%s", namespace, id));
  }

  public HumanSettlement save(HumanSettlement humanSettlement) throws HttpException {
    return doPostRequestForObject("/v2/human_settlements", (HumanSettlementImpl) humanSettlement);
  }

  public HumanSettlement update(UUID uuid, HumanSettlement humanSettlement) throws HttpException {
    return doPutRequestForObject(
        String.format("/v2/human_settlements/%s", uuid), (HumanSettlementImpl) humanSettlement);
  }
}
