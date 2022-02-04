package de.digitalcollections.cudami.client.identifiable.entity.relation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiEntityRelationsClient extends BaseRestClient<EntityRelation> {

  public CudamiEntityRelationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, EntityRelation.class, mapper, "/v5/entities/relations");
  }

  public PageResponse<EntityRelation> getRelationsByPredicate(
      String predicate, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("%s?predicate=%s", baseEndpoint, predicate),
        pageRequest,
        EntityRelation.class);
  }

  public List<EntityRelation> saveRelations(List relations) throws HttpException {
    return doPutRequestForObjectList("/v5/entities/relations", relations, EntityRelation.class);
  }
}
