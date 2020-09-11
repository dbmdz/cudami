package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.CorporationImpl;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiCorporationsClient extends CudamiBaseClient<CorporationImpl> {

  public CudamiCorporationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CorporationImpl.class, mapper);
  }

  public Corporation create() {
    return new CorporationImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/corporations/count"));
  }

  public PageResponse<CorporationImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/corporations", pageRequest);
  }

  public Corporation findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/corporations/%s", uuid));
  }

  public Corporation findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/corporations/%s?pLocale=%s", uuid, locale));
  }

  public Corporation findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/corporations/identifier/%s:%s.json", namespace, id));
  }

  public Corporation fetchAndSaveByGndId(String gndId) throws HttpException {
    return doPostRequestForObject(String.format("/latest/corporations/gnd/%s", gndId));
  }

  public Corporation save(Corporation corporation) throws HttpException {
    return doPostRequestForObject("/latest/corporations", (CorporationImpl) corporation);
  }

  public Corporation update(UUID uuid, Corporation corporation) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/corporations/%s", uuid), (CorporationImpl) corporation);
  }
}
