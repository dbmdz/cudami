package de.digitalcollections.cudami.client;

import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.CorporationImpl;
import java.util.UUID;

public class CudamiCorporationsClient extends CudamiBaseClient<CorporationImpl> {

  public CudamiCorporationsClient(String serverUrl) {
    super(serverUrl, CorporationImpl.class);
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

  public Corporation save(Corporation corporation) throws HttpException {
    return doPostRequestForObject("/latest/corporations", (CorporationImpl) corporation);
  }

  public Corporation update(UUID uuid, Corporation corporation) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/corporations/%s", uuid), (CorporationImpl) corporation);
  }
}
