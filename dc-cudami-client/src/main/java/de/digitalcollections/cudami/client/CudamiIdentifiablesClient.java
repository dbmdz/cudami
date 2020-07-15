package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
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

public interface CudamiIdentifiablesClient {

  public static CudamiIdentifiablesClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiIdentifiablesClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiIdentifiablesClient.class, serverUrl);
    return backend;
  }

  //  default Identifiable create() {
  //    return new IdentifiableImpl();
  //  }
  //
  //  default PageResponse<Identifiable> find(PageRequest pageRequest) {
  //    FindParams f = getFindParams(pageRequest);
  //    PageResponse<Identifiable> pageResponse =
  //        find(
  //            f.getPageNumber(),
  //            f.getPageSize(),
  //            f.getSortField(),
  //            f.getSortDirection(),
  //            f.getNullHandling());
  //    return getGenericPageResponse(pageResponse);
  //  }
  //
  //  default SearchPageResponse<Identifiable> find(SearchPageRequest searchPageRequest) {
  //    FindParams f = getFindParams(searchPageRequest);
  //    SearchPageResponse<Identifiable> pageResponse =
  //        find(
  //            searchPageRequest.getQuery(),
  //            f.getPageNumber(),
  //            f.getPageSize(),
  //            f.getSortField(),
  //            f.getSortDirection(),
  //            f.getNullHandling());
  //    SearchPageResponse<Identifiable> response =
  //        (SearchPageResponse<Identifiable>) getGenericPageResponse(pageResponse);
  //    response.setQuery(searchPageRequest.getQuery());
  //    return response;
  //  }
  //
  //  /**
  //   * Wrapper for find params
  //   *
  //   * @param pageRequest source for find params
  //   * @return wrapped find params
  //   */
  //  default FindParams getFindParams(PageRequest pageRequest) {
  //    int pageNumber = pageRequest.getPageNumber();
  //    int pageSize = pageRequest.getPageSize();
  //
  //    Sorting sorting = pageRequest.getSorting();
  //    Iterator<Order> iterator = sorting.iterator();
  //
  //    String sortField = "";
  //    String sortDirection = "";
  //    String nullHandling = "";
  //
  //    if (iterator.hasNext()) {
  //      Order order = iterator.next();
  //      sortField = order.getProperty() == null ? "" : order.getProperty();
  //      sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
  //      nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
  //    }
  //
  //    return new FindParamsImpl(pageNumber, pageSize, sortField, sortDirection, nullHandling);
  //  }
  //
  //  default PageResponse<Identifiable> getGenericPageResponse(PageResponse pageResponse) {
  //    PageResponse<Identifiable> genericPageResponse;
  //    if (pageResponse.hasContent()) {
  //      List<Identifiable> content = pageResponse.getContent();
  //      List<Identifiable> genericContent = content.stream().collect(Collectors.toList());
  //      genericPageResponse = (PageResponse<Identifiable>) pageResponse;
  //      genericPageResponse.setContent(genericContent);
  //    } else {
  //      genericPageResponse = (PageResponse<Identifiable>) pageResponse;
  //    }
  //    return genericPageResponse;
  //  }

  @RequestLine("GET /v2/identifiables/{uuid}")
  Identifiable getIdentifiable(@Param("uuid") String uuid) throws HttpException;

  @RequestLine("GET /V2/identifiables/identifier/{namespace}:{id}")
  Identifiable getByIdentifier(@Param("namespace") String namespace, @Param("id") String id)
      throws HttpException;

  @RequestLine(
      "GET /latest/identifiables?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Identifiable> find(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine(
      "GET /latest/identifiables/search?searchTerm={searchTerm}&pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  SearchPageResponse<Identifiable> find(
      @Param("searchTerm") String searchTerm,
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/identifiables?searchTerm={searchTerm}&maxResults={maxResults}")
  List<Identifiable> find(
      @Param("searchTerm") String searchTerm, @Param("maxResults") int maxResults);

  @RequestLine("GET /latest/identifiables/{uuid}")
  IdentifiableImpl findOne(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/identifiables/{uuid}?locale={locale}")
  Identifiable findOne(@Param("uuid") UUID uuid, @Param("locale") String locale);

  @RequestLine("GET /latest/identifiables/identifier/{namespace}:{id}.json")
  @Headers("Accept: application/json")
  Identifiable findOneByIdentifier(@Param("namespace") String namespace, @Param("id") String id);

  @RequestLine("POST /latest/identifiables")
  @Headers("Content-Type: application/json")
  Identifiable save(Identifiable identifiable);

  @RequestLine("PUT /latest/identifiables/{uuid}")
  @Headers("Content-Type: application/json")
  Identifiable update(@Param("uuid") UUID uuid, Identifiable identifiable);

  @RequestLine("GET /latest/identifiables/count")
  long count();
}
