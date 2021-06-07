package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiEntitiesClient extends CudamiBaseClient<Entity> {

  public CudamiEntitiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Entity.class, mapper);
  }

  public void addRelatedFileresource(UUID uuid, UUID fileResourceUuid) throws HttpException {
    doPostRequestForObject(
        String.format("/v5/entities/%s/related/fileresources/%s", uuid, fileResourceUuid),
        (Entity) null);
  }

  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws HttpException {
    doPostRequestForObject(
        String.format(
            "/v5/entities/relations/%s/%s/%s", subjectEntityUuid, predicate, objectEntityUuid),
        (Entity) null);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/entities/count"));
  }

  public Entity create() {
    return new Entity();
  }

  public PageResponse<Entity> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/entities", pageRequest);
  }

  public SearchPageResponse<Entity> find(SearchPageRequest searchPageRequest) throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/entities/search", searchPageRequest);
  }

  public List<Entity> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<Entity> response = find(searchPageRequest);
    return response.getContent();
  }

  public Entity findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/entities/%s", uuid));
  }

  public Entity findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Entity findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v5/entities/%s?locale=%s", uuid, locale));
  }

  public Entity findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/entities/identifier/%s:%s.json", namespace, id));
  }

  public Entity findOneByRefId(long refId) throws HttpException {
    return doGetRequestForObject(String.format("/v5/entities/%d", refId));
  }

  public List findRandomEntities(int count) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/random?count=%d", count), Entity.class);
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/%s/related/fileresources", uuid), FileResource.class);
  }

  public List<EntityRelation> getRelations(UUID subjectEntityUuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/relations/%s", subjectEntityUuid), EntityRelation.class);
  }

  public Entity save(Entity entity) throws HttpException {
    return doPostRequestForObject("/v5/entities", entity);
  }

  public List<FileResource> saveRelatedFileResources(UUID uuid, List fileResources)
      throws HttpException {
    return doPostRequestForObjectList(
        String.format("/v5/entities/%s/related/fileresources", uuid),
        fileResources,
        FileResource.class);
  }

  public List<EntityRelation> saveRelationsForSubject(List relations) throws HttpException {
    return doPutRequestForObjectList(
        String.format(
            "/v5/entities/%s/relations",
            ((EntityRelation) relations.get(0)).getSubject().getUuid()),
        relations,
        EntityRelation.class);
  }

  public Entity update(UUID uuid, Entity entity) throws HttpException {
    return doPutRequestForObject(String.format("/v5/entities/%s", uuid), entity);
  }
}
