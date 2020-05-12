package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import feign.Logger;
import feign.Param;
import feign.ReflectiveFeign;
import feign.RequestLine;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

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

  @RequestLine("GET /v2/identifiables/{uuid}")
  Identifiable getIdentifiable(@Param("uuid") String uuid) throws HttpException;

  @RequestLine("GET /V2/identifiables/identifier/{namespace}:{id}")
  Identifiable getByIdentifier(@Param("namespace") String namespace, @Param("id") String id)
      throws HttpException;
}
