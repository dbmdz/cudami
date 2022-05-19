package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiHeadwordEntriesClient extends CudamiEntitiesClient<HeadwordEntry> {

  public CudamiHeadwordEntriesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, HeadwordEntry.class, mapper, API_VERSION_PREFIX + "/headwordentries");
  }

  public List getByHeadword(UUID headwordUuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/headword/%s", baseEndpoint, headwordUuid), HeadwordEntry.class);
  }
}
