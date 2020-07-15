package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
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

public interface CudamiDigitalObjectsClient {

  public static CudamiDigitalObjectsClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiDigitalObjectsClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiDigitalObjectsClient.class, serverUrl);
    return backend;
  }

  @RequestLine("GET /latest/digitalobjects/count")
  long count();

  //  default DigitalObject create() {
  //    return new DigitalObjectImpl();
  //  }
  //
  //  default PageResponse<DigitalObject> find(PageRequest pageRequest) {
  //    FindParams f = new FindParamsImpl(pageRequest);
  //    PageResponse<DigitalObject> pageResponse =
  //        find(
  //            f.getPageNumber(),
  //            f.getPageSize(),
  //            f.getSortField(),
  //            f.getSortDirection(),
  //            f.getNullHandling());
  //    return pageResponse;
  //  }

  @RequestLine(
      "GET /latest/digitalobjects?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<DigitalObject> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/digitalobjects/search?searchTerm={searchTerm}&maxResults={maxResults}")
  List<DigitalObject> find(
      @Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine(
      "GET /latest/digitalobjects/search?searchTerm={searchTerm}&pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  SearchPageResponse<DigitalObject> find(
      @Param("searchTerm") String searchTerm,
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/digitalobjects/{uuid}")
  DigitalObject findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/digitalobjects/{uuid}?locale={locale}")
  DigitalObject findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/digitalobjects/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  DigitalObject findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("GET /latest/digitalobjects/{uuid}/fileresources")
  List<FileResource> getFileResources(@Param("uuid") UUID uuid);

  // http://localhost:9000/latest/digitalobjects/8f543eca-da48-4d21-854a-0c0158110f9b/fileresources/images
  @RequestLine("GET /latest/digitalobjects/{uuid}/fileresources/images")
  List<ImageFileResource> getImageFileResources(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/digitalobjects")
  @Headers("Content-Type: application/json")
  DigitalObject save(DigitalObject digitalObject);

  @RequestLine("POST /latest/digitalobjects/{uuid}/fileresources")
  List<FileResource> saveFileResources(@Param("uuid") UUID uuid, List<FileResource> fileResources);

  @RequestLine("PUT /latest/digitalobjects/{uuid}")
  @Headers("Content-Type: application/json")
  DigitalObject update(@Param("uuid") UUID uuid, DigitalObject digitalObject);
}
