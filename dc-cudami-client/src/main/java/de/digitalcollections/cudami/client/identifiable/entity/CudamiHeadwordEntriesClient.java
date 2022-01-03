package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiHeadwordEntriesClient extends CudamiBaseClient<HeadwordEntry> {

  public CudamiHeadwordEntriesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, HeadwordEntry.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/headwordentries/count"));
  }

  public HeadwordEntry create() {
    return new HeadwordEntry();
  }

  public PageResponse<HeadwordEntry> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/headwordentries", pageRequest);
  }

  public SearchPageResponse<HeadwordEntry> find(SearchPageRequest pageRequest)
      throws HttpException {
    return this.doGetSearchRequestForPagedObjectList("/v5/headwordentries", pageRequest);
  }

  public List findByHeadword(UUID headwordUuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/headwordentries/headword/%s", headwordUuid), HeadwordEntry.class);
  }

  public HeadwordEntry findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/headwordentries/%s", uuid));
  }

  public HeadwordEntry findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/headwordentries/identifier/%s:%s.json", namespace, id));
  }

  public HeadwordEntry save(HeadwordEntry headwordEntry) throws HttpException {
    return doPostRequestForObject("/v5/headwordentries", headwordEntry);
  }

  public HeadwordEntry update(UUID uuid, HeadwordEntry headwordEntry) throws HttpException {
    return doPutRequestForObject(String.format("/v5/headwordentries/%s", uuid), headwordEntry);
  }
}
