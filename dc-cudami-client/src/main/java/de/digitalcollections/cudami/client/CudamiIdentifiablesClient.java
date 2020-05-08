package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageResponse;
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
import java.util.Locale;
import java.util.Map;
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

  @RequestLine("GET /v3/webpages/{uuid}")
  Webpage getWebpage(@Param("uuid") String uuid) throws HttpException;

  @RequestLine("GET /v3/webpages/{uuid}?pLocale={locale}")
  Webpage getWebpage(@Param("locale") Locale locale, @Param("uuid") String uuid)
      throws HttpException;

  @RequestLine("GET /v3/webpages/{uuid}/children")
  PageResponse<Webpage> getChildren(@Param("uuid") UUID uuid, @QueryMap Map queryMap);

  @RequestLine("GET /V2/websites/{uuid}")
  Website getWebsite(@Param("uuid") String uuid) throws HttpException;
}
