package de.digitalcollections.cudami.client;

import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiIdentifiablesClient extends CudamiBaseClient<IdentifiableImpl> {

  public CudamiIdentifiablesClient(String serverUrl) {
    super(serverUrl, IdentifiableImpl.class);
  }

  public Identifiable create() {
    return new IdentifiableImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/identifiables/count"));
  }

  public PageResponse<IdentifiableImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/identifiables", pageRequest);
  }

  public SearchPageResponse<IdentifiableImpl> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/identifiables/search", searchPageRequest);
  }

  public List<IdentifiableImpl> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<IdentifiableImpl> response = find(searchPageRequest);
    return response.getContent();
  }

  public Identifiable findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/identifiables/%s", uuid));
  }

  public Identifiable findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/identifiables/identifier/%s:%s.json", namespace, id));
  }

  public Identifiable findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Identifiable findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/identifiables/%s?locale=%s", uuid, locale));
  }

  public Identifiable save(Identifiable identifiable) throws HttpException {
    return doPostRequestForObject("/latest/identifiables", (IdentifiableImpl) identifiable);
  }

  public Identifiable update(UUID uuid, Identifiable identifiable) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/identifiables/%s", uuid), (IdentifiableImpl) identifiable);
  }
}
