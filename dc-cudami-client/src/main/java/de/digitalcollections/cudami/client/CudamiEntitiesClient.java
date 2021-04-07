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
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.relation.EntityRelationImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiEntitiesClient extends CudamiBaseClient<EntityImpl> {

  public CudamiEntitiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, EntityImpl.class, mapper);
  }

  public void addRelatedFileresource(UUID uuid, UUID fileResourceUuid) throws HttpException {
    // No POST endpoint for /latest/entities/%s/related/fileresources/%s available!
    throw new HttpException(
        String.format("/latest/entities/%s/related/fileresources/%s", uuid, fileResourceUuid), 404);
  }

  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws HttpException {
    // No POST endpoint for /latest/entities/relations/%s/%s/%s available
    throw new HttpException(
        String.format(
            "/latest/entities/relations/%s/%s/%s", subjectEntityUuid, predicate, objectEntityUuid),
        404);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v3/entities/count"));
  }

  public Entity create() {
    return new EntityImpl();
  }

  public PageResponse<EntityImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v3/entities", pageRequest);
  }

  public SearchPageResponse<EntityImpl> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v2/entities/search", searchPageRequest);
  }

  public List<EntityImpl> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<EntityImpl> response = find(searchPageRequest);
    return response.getContent();
  }

  public Entity findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/entities/%s", uuid));
  }

  public Entity findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Entity findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/entities/%s?locale=%s", uuid, locale));
  }

  public Entity findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v3/entities/identifier/%s:%s.json", namespace, id));
  }

  public Entity findOneByRefId(long refId) throws HttpException {
    return doGetRequestForObject(String.format("/v3/entities/%d", refId));
  }

  public List findRandomEntities(int count) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/entities/random?count=%d", count), EntityImpl.class);
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public List<EntityRelation> getRelations(UUID subjectEntityUuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/entities/relations/%s", subjectEntityUuid), EntityRelationImpl.class);
  }

  public Entity save(Entity entity) throws HttpException {
    // No POST endpoint for /latest/entities available!
    throw new HttpException("/latest/entities", 404);
  }

  public List<FileResource> saveRelatedFileResources(UUID uuid, List fileResources)
      throws HttpException {
    // No POST endpoint for /latest/entities/%s/related/fileresources available!
    throw new HttpException(String.format("/latest/entities/%s/related/fileresources", uuid), 404);
  }

  public List<EntityRelation> saveRelationsForSubject(List relations) throws HttpException {
    return doPutRequestForObjectList(
        String.format(
            "/v3/entities/%s/relations",
            ((EntityRelation) relations.get(0)).getSubject().getUuid()),
        relations,
        EntityRelationImpl.class);
  }

  public Entity update(UUID uuid, Entity entity) throws HttpException {
    // No PUT endpoint for /latest/entities/%s available!
    throw new HttpException(String.format("/latest/entities/%s", uuid), 404);
  }
}
