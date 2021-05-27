package de.digitalcollections.cudami.client.identifiable.entity.relation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiEntityRelationsClient extends CudamiBaseClient {

  public CudamiEntityRelationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, EntityRelation.class, mapper);
  }

  public PageResponse<EntityRelation> getRelationsByPredicate(
      String predicate, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/entities/relations?predicate=%s", predicate),
        pageRequest,
        EntityRelation.class);
  }

  public List<EntityRelation> saveRelations(List<EntityRelation> relations) throws HttpException {
    return doPutRequestForObjectList("/v5/entities/relations", relations, EntityRelation.class);
  }
}
