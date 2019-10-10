package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import feign.Logger;
import feign.Param;
import feign.ReflectiveFeign;
import feign.RequestLine;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.List;
import java.util.Locale;

public interface CudamiClient {

  public static CudamiClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiClient.class, serverUrl);
    return backend;
  }

  @RequestLine("GET /v2/languages/default")
  String getDefaultLanguage() throws HttpException;

  @RequestLine("GET /v2/locales/default")
  Locale getDefaultLocale() throws HttpException;

  @RequestLine("GET /v2/languages")
  List<String> getSupportedLanguages() throws HttpException;

  @RequestLine("GET /v2/locales")
  List<Locale> getSupportedLocales() throws HttpException;

  @RequestLine("GET /v2/webpages/{uuid}")
  Webpage getWebpage(@Param("uuid") String uuid) throws HttpException;

  @RequestLine("GET /v2/webpages/{uuid}?pLocale={locale}")
  Webpage getWebpage(@Param("locale") Locale locale, @Param("uuid") String uuid)
      throws HttpException;

  @RequestLine("GET /V2/websites/{uuid}")
  Website getWebsite(@Param("uuid") String uuid) throws HttpException;
}
