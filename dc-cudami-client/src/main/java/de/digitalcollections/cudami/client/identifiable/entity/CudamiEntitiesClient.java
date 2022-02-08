package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiEntitiesClient<E extends Entity> extends CudamiIdentifiablesClient<E> {

  public CudamiEntitiesClient(
      HttpClient http,
      String serverUrl,
      Class<E> entityClass,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, entityClass, mapper, baseEndpoint);
  }

  public CudamiEntitiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    this(http, serverUrl, (Class<E>) Entity.class, mapper, "/v5/entities");
  }

  public void addRelatedFileresource(UUID uuid, UUID fileResourceUuid) throws TechnicalException {
    doPostRequestForObject(
        String.format("/v5/entities/%s/related/fileresources/%s", uuid, fileResourceUuid),
        (E) null);
  }

  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws TechnicalException {
    doPostRequestForObject(
        String.format(
            "/v5/entities/relations/%s/%s/%s", subjectEntityUuid, predicate, objectEntityUuid),
        (E) null);
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link CudamiEntitiesClient#getByRefId(long)} instead.
   * @param refId unique reference id of object
   * @return object with given refId
   */
  @Deprecated(forRemoval = true)
  public E findOneByRefId(long refId) throws TechnicalException {
    return getByRefId(refId);
  }

  public List findRandomEntities(int count) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/random?count=%d", count), Entity.class);
  }

  public E getByRefId(long refId) throws TechnicalException {
    return doGetRequestForObject(String.format("%s/%d", baseEndpoint, refId));
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/%s/related/fileresources", uuid), FileResource.class);
  }

  public List<EntityRelation> getRelations(UUID subjectEntityUuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/relations/%s", subjectEntityUuid), EntityRelation.class);
  }

  public List<FileResource> saveRelatedFileResources(UUID uuid, List fileResources)
      throws TechnicalException {
    return doPostRequestForObjectList(
        String.format("/v5/entities/%s/related/fileresources", uuid),
        fileResources,
        FileResource.class);
  }

  public List<EntityRelation> saveRelationsForSubject(List relations) throws TechnicalException {
    return doPutRequestForObjectList(
        String.format(
            "/v5/entities/%s/relations",
            ((EntityRelation) relations.get(0)).getSubject().getUuid()),
        relations,
        EntityRelation.class);
  }
}
