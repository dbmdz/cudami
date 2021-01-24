package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.EntityPartImpl;
import de.digitalcollections.model.impl.identifiable.entity.relation.EntityRelationImpl;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiEntityRelationsClient extends CudamiBaseClient {

  public CudamiEntityRelationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, EntityPartImpl.class, mapper);
  }

  public PageResponse<EntityRelation> getRelationsByPredicate(
      String predicate, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/entities/relations?predicate=%s", predicate),
        pageRequest,
        EntityRelationImpl.class);
  }

  public List<EntityRelation> saveRelations(List<EntityRelation> relations) throws HttpException {
    return doPutRequestForObjectList(
        "/latest/entities/relations", relations, EntityRelationImpl.class);
  }
}
