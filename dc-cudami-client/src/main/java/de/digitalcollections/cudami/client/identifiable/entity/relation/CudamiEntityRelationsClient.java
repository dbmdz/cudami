package de.digitalcollections.cudami.client.identifiable.entity.relation;

import static de.digitalcollections.cudami.client.CudamiRestClient.API_VERSION_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.relation.EntityToEntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiEntityRelationsClient extends BaseRestClient<EntityToEntityRelation> {

  public CudamiEntityRelationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(
        http,
        serverUrl,
        EntityToEntityRelation.class,
        mapper,
        API_VERSION_PREFIX + "/entities/relations");
  }

  public PageResponse<EntityToEntityRelation> findByPredicate(
      String predicate, PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s?predicate=%s", baseEndpoint, predicate),
        pageRequest,
        EntityToEntityRelation.class);
  }

  public List<EntityToEntityRelation> save(List relations) throws TechnicalException {
    return doPutRequestForObjectList(
        API_VERSION_PREFIX + "/entities/relations", relations, EntityToEntityRelation.class);
  }
}
