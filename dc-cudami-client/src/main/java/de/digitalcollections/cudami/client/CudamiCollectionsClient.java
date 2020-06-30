package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.FindParams;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.paging.FindParamsImpl;
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
import java.util.UUID;

public interface CudamiCollectionsClient {

  public static CudamiCollectionsClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiCollectionsClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiCollectionsClient.class, serverUrl);
    return backend;
  }

  default Collection createCollection() {
    return new CollectionImpl();
  }

  default PageResponse findTopCollections(PageRequest pageRequest) {
    FindParams f = new FindParamsImpl(pageRequest);
    PageResponse<Collection> pageResponse =
        findTopCollections(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return pageResponse;
  }

  default PageResponse findCollections(PageRequest pageRequest) {
    FindParams f = new FindParamsImpl(pageRequest);
    PageResponse<Collection> pageResponse =
        findCollections(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return pageResponse;
  }

  @RequestLine(
      "GET /latest/collections?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Collection> findCollections(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine(
      "GET /latest/collections/top?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Collection> findTopCollections(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/collections/{uuid}")
  Collection getCollection(@Param("uuid") UUID uuid) throws HttpException;

  @RequestLine("POST /latest/collections/{parentCollectionUuid}/collection")
  @Headers("Content-Type: application/json")
  Collection saveCollectionWithParentCollection(
      Collection collection, @Param("parentCollectionUuid") UUID parentCollectionUuid);

  @RequestLine("POST /latest/collections")
  @Headers("Content-Type: application/json")
  Collection saveCollection(Collection collection);

  default Collection updateCollection(Collection collection) {
    return updateCollection(collection.getUuid(), collection);
  }

  @RequestLine("PUT /latest/collections/{uuid}")
  @Headers("Content-Type: application/json")
  Collection updateCollection(@Param("uuid") UUID uuid, Collection collection);

  @RequestLine("GET /latest/collections/{uuid}/breadcrumb")
  BreadcrumbNavigation getBreadcrumbNavigation(@Param("uuid") UUID collectionUuid);
}
