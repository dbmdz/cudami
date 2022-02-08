package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiHeadwordEntriesClient extends CudamiEntitiesClient<HeadwordEntry> {

  public CudamiHeadwordEntriesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, HeadwordEntry.class, mapper, "/v5/headwordentries");
  }

  @Override
  public SearchPageResponse<HeadwordEntry> find(SearchPageRequest pageRequest)
      throws TechnicalException {
    return this.doGetSearchRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public List findByHeadword(UUID headwordUuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/headword/%s", baseEndpoint, headwordUuid), HeadwordEntry.class);
  }
}
