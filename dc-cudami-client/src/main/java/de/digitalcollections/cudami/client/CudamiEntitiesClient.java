package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.entity.Entity;
import de.digitalcollections.model.impl.identifiable.entity.relation.EntityRelationImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiEntitiesClient extends CudamiBaseClient<Entity> {

  public CudamiEntitiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Entity.class, mapper);
  }

  public void addRelatedFileresource(UUID uuid, UUID fileResourceUuid) throws HttpException {
    doPostRequestForObject(String.format("/latest/entities/%s/related/fileresources/%s", uuid, fileResourceUuid),
        (Entity) null);
  }

  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws HttpException {
    doPostRequestForObject(String.format(
            "/latest/entities/relations/%s/%s/%s", subjectEntityUuid, predicate, objectEntityUuid),
        (Entity) null);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/entities/count"));
  }

  public Entity create() {
    return new Entity();
  }

  public PageResponse<Entity> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/entities", pageRequest);
  }

  public SearchPageResponse<Entity> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/entities/search", searchPageRequest);
  }

  public List<Entity> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<Entity> response = find(searchPageRequest);
    return response.getContent();
  }

  public Entity findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/entities/%s", uuid));
  }

  public Entity findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Entity findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/entities/%s?locale=%s", uuid, locale));
  }

  public Entity findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/entities/identifier/%s:%s.json", namespace, id));
  }

  public Entity findOneByRefId(long refId) throws HttpException {
    return doGetRequestForObject(String.format("/latest/entities/%d", refId));
  }

  public List findRandomEntities(int count) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/entities/random?count=%d", count), Entity.class);
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public List<EntityRelation> getRelations(UUID subjectEntityUuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/entities/relations/%s", subjectEntityUuid),
        EntityRelationImpl.class);
  }

  public Entity save(Entity entity) throws HttpException {
    return doPostRequestForObject("/latest/entities", (Entity) entity);
  }

  public List<FileResource> saveRelatedFileResources(UUID uuid, List fileResources)
      throws HttpException {
    return doPostRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid),
        fileResources,
        FileResourceImpl.class);
  }

  public List<EntityRelation> saveRelationsForSubject(List relations) throws HttpException {
    return doPutRequestForObjectList(
        String.format(
            "/latest/entities/%s/relations",
            ((EntityRelation) relations.get(0)).getSubject().getUuid()),
        relations,
        EntityRelationImpl.class);
  }

  public Entity update(UUID uuid, Entity entity) throws HttpException {
    return doPutRequestForObject(String.format("/latest/entities/%s", uuid), (Entity) entity);
  }
}
