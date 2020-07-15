package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.paging.PageResponse;
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

public interface CudamiCorporationsClient {

  public static CudamiCorporationsClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiCorporationsClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiCorporationsClient.class, serverUrl);
    return backend;
  }

  //  default Corporation createCorporation() {
  //    return new CorporationImpl();
  //  }
  //
  //  default PageResponse findCorporations(PageRequest pageRequest) {
  //    FindParams f = new FindParamsImpl(pageRequest);
  //    PageResponse<Corporation> pageResponse =
  //        findCorporations(
  //            f.getPageNumber(),
  //            f.getPageSize(),
  //            f.getSortField(),
  //            f.getSortDirection(),
  //            f.getNullHandling());
  //    return pageResponse;
  //  }

  @RequestLine(
      "GET /latest/corporations?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Corporation> findCorporations(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/corporations/{uuid}")
  Corporation getCorporation(@Param("uuid") UUID uuid) throws HttpException;

  @RequestLine("POST /latest/corporations/{parentCorporationUuid}/corporation")
  @Headers("Content-Type: application/json")
  Corporation saveCorporationWithParentCorporation(
      Corporation corporation, @Param("parentCorporationUuid") UUID parentCorporationUuid);

  @RequestLine("POST /latest/corporations")
  @Headers("Content-Type: application/json")
  Corporation saveCorporation(Corporation corporation);

  default Corporation updateCorporation(Corporation corporation) {
    return updateCorporation(corporation.getUuid(), corporation);
  }

  @RequestLine("PUT /latest/corporations/{uuid}")
  @Headers("Content-Type: application/json")
  Corporation updateCorporation(@Param("uuid") UUID uuid, Corporation corporation);
}
