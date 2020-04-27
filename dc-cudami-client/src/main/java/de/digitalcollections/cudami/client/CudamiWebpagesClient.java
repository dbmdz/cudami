package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.feign.expander.paging.PageRequestToUrlParamsExpander;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import feign.Logger;
import feign.Param;
import feign.QueryMap;
import feign.ReflectiveFeign;
import feign.RequestLine;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.Map;
import java.util.UUID;

public interface CudamiWebpagesClient {

  public static CudamiWebpagesClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiWebpagesClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiWebpagesClient.class, serverUrl);
    return backend;
  }

  @RequestLine("GET /latest/webpages?{pageRequestParams}")
  PageResponse<Webpage> find(
      @Param(value = "pageRequestParams", expander = PageRequestToUrlParamsExpander.class)
          PageRequest pageRequest);

  @RequestLine("GET /latest/webpages/{uuid}/children")
  PageResponse<Webpage> getChildren(@Param("uuid") UUID uuid, @QueryMap Map queryMap);
}
