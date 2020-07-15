package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.ReflectiveFeign;
import feign.RequestLine;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.List;
import java.util.UUID;

public interface CudamiEntitiesClient {

  public static CudamiEntitiesClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiEntitiesClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiEntitiesClient.class, serverUrl);
    return backend;
  }

  //  default PageResponse<Entity> find(PageRequest pageRequest) {
  //    FindParams f = new FindParamsImpl(pageRequest);
  //    PageResponse<Entity> pageResponse =
  //        find(
  //            f.getPageNumber(),
  //            f.getPageSize(),
  //            f.getSortField(),
  //            f.getSortDirection(),
  //            f.getNullHandling());
  //    return pageResponse;
  //  }
  //
  //  default SearchPageResponse<Entity> find(SearchPageRequest searchPageRequest) {
  //    FindParams f = getFindParams(searchPageRequest);
  //    SearchPageResponse<Entity> pageResponse =
  //        find(
  //            searchPageRequest.getQuery(),
  //            f.getPageNumber(),
  //            f.getPageSize(),
  //            f.getSortField(),
  //            f.getSortDirection(),
  //            f.getNullHandling());
  //    SearchPageResponse<Entity> response =
  //        (SearchPageResponse<Entity>) getGenericPageResponse(pageResponse);
  //    response.setQuery(searchPageRequest.getQuery());
  //    return response;
  //  }

  //  default void addRelatedFileresource(Entity entity, FileResource fileResource) {
  //    addRelatedFileresource(entity.getUuid(), fileResource.getUuid());
  //  }
  //
  //  default void addRelation(EntityRelation<Entity> relation) {
  //    addRelation(
  //        relation.getSubject().getUuid(), relation.getPredicate(),
  // relation.getObject().getUuid());
  //  }
  //
  //  default List<FileResource> getRelatedFileResources(Entity entity) {
  //    return getRelatedFileResources(entity.getUuid());
  //  }
  //
  //  default List<EntityRelation> getRelations(Entity subjectEntity) {
  //    return getRelations(subjectEntity.getUuid());
  //  }
  //
  //  default List<FileResource> saveRelatedFileResources(
  //      Entity entity, List<FileResource> fileResources) {
  //    return saveRelatedFileResources(entity.getUuid(), fileResources);
  //  }
  //
  //  default Entity create() {
  //    return new EntityImpl();
  //  }

  @RequestLine("GET /latest/entities/count")
  long count();

  @RequestLine("POST /latest/entities/{uuid}/related/fileresources/{fileResourceUuid}")
  void addRelatedFileresource(
      @Param("uuid") UUID uuid, @Param("fileResourceUuid") UUID fileResourceUuid);

  @RequestLine("POST /latest/entities/relations/{subjectEntityUuid}/{predicate}/{objectEntityUuid}")
  public void addRelation(
      @Param("subjectEntityUuid") UUID subjectEntityUuid,
      @Param("predicate") String predicate,
      @Param("objectEntityUuid") UUID objectEntityUuid);

  @RequestLine(
      "GET /latest/entities?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Entity> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/entities?searchTerm={searchTerm}&maxResults={maxResults}")
  List<Entity> find(@Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine(
      "GET /latest/entities?searchTerm={searchTerm}&pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  SearchPageResponse<Entity> find(
      @Param("searchTerm") String searchTerm,
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/entities/{uuid}")
  Entity findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/entities/{uuid}?locale={locale}")
  Entity findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/entities/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  Entity findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("GET /latest/entities/{refId}")
  @Headers("Accept: application/json")
  Entity findOneByRefId(@Param("refId") long refId);

  @RequestLine("GET /latest/entities/{uuid}/related/fileresources")
  List<FileResource> getRelatedFileResources(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/entities/relations/{subjectEntityUuid}")
  List<EntityRelation> getRelations(@Param("subjectEntityUuid") UUID subjectEntityUuid);

  @RequestLine("POST /latest/entities")
  @Headers("Content-Type: application/json")
  Entity save(Entity entity);

  @RequestLine("POST /latest/entities/{uuid}/related/fileresources")
  List<FileResource> saveRelatedFileResources(
      @Param("uuid") UUID uuid, List<FileResource> fileResources);

  @RequestLine("POST /latest/entities/relations")
  List<EntityRelation> saveRelations(List<EntityRelation> relations);

  @RequestLine("PUT /latest/entities/{uuid}")
  @Headers("Content-Type: application/json")
  Entity update(@Param("uuid") UUID uuid, Entity entity);
}
