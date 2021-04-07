package de.digitalcollections.cudami.client.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiCorporateBodiesClient extends CudamiBaseClient<CorporateBodyImpl> {

  public CudamiCorporateBodiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CorporateBodyImpl.class, mapper);
  }

  public CorporateBody create() {
    return new CorporateBodyImpl();
  }

  public long count() throws HttpException {
    return -1; // URL /latest/corporatebodies/count does not exist
  }

  public PageResponse<CorporateBodyImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/corporatebodies", pageRequest);
  }

  public CorporateBody findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/corporatebodies/%s", uuid));
  }

  public CorporateBody findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/corporatebodies/%s?pLocale=%s", uuid, locale));
  }

  public CorporateBody findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v3/corporatebodies/identifier/%s:%s.json", namespace, id));
  }

  public CorporateBody fetchAndSaveByGndId(String gndId) throws HttpException {
    return doPostRequestForObject(String.format("/v3/corporatebodies/gnd/%s", gndId));
  }

  public CorporateBody save(CorporateBody corporateBody) throws HttpException {
    return doPostRequestForObject("/v2/corporatebodies", (CorporateBodyImpl) corporateBody);
  }

  public CorporateBody update(UUID uuid, CorporateBody corporateBody) throws HttpException {
    return doPutRequestForObject(
        String.format("/v2/corporatebodies/%s", uuid), (CorporateBodyImpl) corporateBody);
  }
}
