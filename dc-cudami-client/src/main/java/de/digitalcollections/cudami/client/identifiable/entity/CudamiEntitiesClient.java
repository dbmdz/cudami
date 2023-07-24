package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiEntitiesClient<E extends Entity> extends CudamiIdentifiablesClient<E> {

  private static final String BASE_ENDPOINT_ENTITIES = API_VERSION_PREFIX + "/entities";

  public CudamiEntitiesClient(
      HttpClient http,
      String serverUrl,
      Class<E> entityClass,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, entityClass, mapper, baseEndpoint);
  }

  public CudamiEntitiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    this(http, serverUrl, (Class<E>) Entity.class, mapper, BASE_ENDPOINT_ENTITIES);
  }

  public void addRelatedFileresource(UUID uuid, UUID fileResourceUuid) throws TechnicalException {
    doPostRequestForObject(
        String.format(
            "%s/%s/related/fileresources/%s", BASE_ENDPOINT_ENTITIES, uuid, fileResourceUuid),
        (E) null);
  }

  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws TechnicalException {
    doPostRequestForObject(
        String.format(
            "%s/relations/%s/%s/%s",
            BASE_ENDPOINT_ENTITIES, subjectEntityUuid, predicate, objectEntityUuid),
        (E) null);
  }

  public List<EntityRelation> addRelationsForSubject(List relations) throws TechnicalException {
    return doPutRequestForObjectList(
        String.format(
            "%s/%s/relations",
            BASE_ENDPOINT_ENTITIES, ((EntityRelation) relations.get(0)).getSubject().getUuid()),
        relations,
        EntityRelation.class);
  }

  public E getByRefId(long refId) throws TechnicalException {
    try {
      return doGetRequestForObject(String.format("%s/%d", baseEndpoint, refId));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public List getRandomEntities(int count) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/random?count=%d", BASE_ENDPOINT_ENTITIES, count), Entity.class);
  }

  @Deprecated(forRemoval = true)
  public List<FileResource> getRelatedFileResources(UUID uuid) throws TechnicalException {
    PageResponse<FileResource> pageResponse =
        getRelatedFileResources(uuid, PageRequest.builder().pageNumber(0).pageSize(1000).build());
    return pageResponse.getContent();
  }

  public PageResponse<FileResource> getRelatedFileResources(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    PageResponse pageResponse =
        doGetRequestForPagedObjectList(
            String.format("%s/%s/related/fileresources", BASE_ENDPOINT_ENTITIES, uuid),
            pageRequest,
            FileResource.class);
    return pageResponse;
  }

  @Deprecated(forRemoval = true)
  public List<EntityRelation> getRelations(UUID subjectEntityUuid) throws TechnicalException {
    PageResponse<EntityRelation> pageResponse =
        getRelations(subjectEntityUuid, PageRequest.builder().pageNumber(0).pageSize(1000).build());
    return pageResponse.getContent();
  }

  public PageResponse<EntityRelation> getRelations(UUID subjectEntityUuid, PageRequest pageRequest)
      throws TechnicalException {
    PageResponse pageResponse =
        doGetRequestForPagedObjectList(
            String.format("%s/relations/%s", BASE_ENDPOINT_ENTITIES, subjectEntityUuid),
            pageRequest,
            EntityRelation.class);
    return pageResponse;
  }

  public List<FileResource> setRelatedFileResources(UUID uuid, List fileResources)
      throws TechnicalException {
    // FIXME: endoint on server side missing?
    return doPostRequestForObjectList(
        String.format("%s/%s/related/fileresources", BASE_ENDPOINT_ENTITIES, uuid),
        fileResources,
        FileResource.class);
  }
}
