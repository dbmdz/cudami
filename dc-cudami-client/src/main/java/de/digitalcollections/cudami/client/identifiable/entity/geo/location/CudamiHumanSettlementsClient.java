package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiHumanSettlementsClient extends CudamiBaseClient<HumanSettlement> {

  public CudamiHumanSettlementsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, HumanSettlement.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/human_settlements/count"));
  }

  public HumanSettlement create() {
    return new HumanSettlement();
  }

  public PageResponse<HumanSettlement> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/human_settlements", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
          PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/latest/human_settlements", pageRequest, language, initial);
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
            "/latest/human_settlements",
            pageNumber,
            pageSize,
            sortField,
            sortDirection,
            nullHandling,
            language,
            initial);
  }

  public HumanSettlement findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/human_settlements/%s", uuid));
  }

  public HumanSettlement findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
            String.format("/latest/human_settlements/identifier?namespace=%s&id=%s", namespace, id));
  }

  public HumanSettlement save(HumanSettlement humanSettlement) throws HttpException {
    return doPostRequestForObject("/latest/human_settlements", humanSettlement);
  }

  public HumanSettlement update(UUID uuid, HumanSettlement humanSettlement) throws HttpException {
    return doPutRequestForObject(String.format("/latest/human_settlements/%s", uuid), humanSettlement);
  }
}
