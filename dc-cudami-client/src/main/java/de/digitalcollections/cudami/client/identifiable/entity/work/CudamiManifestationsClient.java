package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiManifestationsClient extends CudamiEntitiesClient<Manifestation> {

  public CudamiManifestationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Manifestation.class, mapper, API_VERSION_PREFIX + "/manifestations");
  }

  public PageResponse<Manifestation> findChildren(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/" + uuid + "/children"), pageRequest);
  }

  public PageResponse<Item> findItems(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/items", baseEndpoint, uuid), pageRequest, Item.class);
  }

  public List<Locale> getLanguagesOfItems(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/items/languages", baseEndpoint, uuid), Locale.class);
  }
}
