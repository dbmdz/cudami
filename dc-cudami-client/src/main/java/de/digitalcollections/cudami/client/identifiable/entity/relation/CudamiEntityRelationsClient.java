package de.digitalcollections.cudami.client.identifiable.entity.relation;

import static de.digitalcollections.cudami.client.CudamiRestClient.API_VERSION_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiEntityRelationsClient extends BaseRestClient<EntityRelation> {

  public CudamiEntityRelationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(
        http, serverUrl, EntityRelation.class, mapper, API_VERSION_PREFIX + "/entities/relations");
  }

  public PageResponse<EntityRelation> findByPredicate(String predicate, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s?predicate=%s", baseEndpoint, predicate),
        pageRequest,
        EntityRelation.class);
  }

  public List<EntityRelation> save(List relations) throws TechnicalException {
    return doPutRequestForObjectList(
        API_VERSION_PREFIX + "/entities/relations", relations, EntityRelation.class);
  }
}
