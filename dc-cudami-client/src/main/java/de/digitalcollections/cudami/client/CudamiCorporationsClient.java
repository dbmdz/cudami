package de.digitalcollections.cudami.client;

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

  public long count() throws Exception {
    return Long.parseLong(doGetRequestForString("/latest/corporations/count"));
  }

  public PageResponse<CorporationImpl> find(PageRequest pageRequest) throws Exception {
    return doGetRequestForPagedObjectList("/latest/corporations", pageRequest);
  }

  public Corporation findOne(UUID uuid) throws Exception {
    return doGetRequestForObject(String.format("/latest/corporations/%s", uuid));
  }

  public Corporation findOne(UUID uuid, String locale) throws Exception {
    return doGetRequestForObject(String.format("/latest/corporations/%s?pLocale=%s", uuid, locale));
  }

  public Corporation findOneByIdentifier(String namespace, String id) throws Exception {
    return doGetRequestForObject(
        String.format("/latest/corporations/identifier/%s:%s.json", namespace, id));
  }

  public Corporation save(Corporation corporation) throws Exception {
    return doPostRequestForObject("/latest/corporations", (CorporationImpl) corporation);
  }

  public Corporation update(UUID uuid, Corporation corporation) throws Exception {
    return doPutRequestForObject(
        String.format("/latest/corporations/%s", uuid), (CorporationImpl) corporation);
  }
}
