package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.impl.identifiable.entity.EntityRelationImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.EntityPartImpl;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiEntityRelationsClient extends CudamiBaseClient {

  public CudamiEntityRelationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, EntityPartImpl.class, mapper);
  }

  public List<EntityRelation> saveRelations(List<EntityRelation> relations) throws HttpException {
    return doPutRequestForObjectList(
        "/latest/entities/relations", relations, EntityRelationImpl.class);
  }
}
