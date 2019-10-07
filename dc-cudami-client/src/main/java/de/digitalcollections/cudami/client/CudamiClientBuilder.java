package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import feign.Logger;
import feign.ReflectiveFeign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

public class CudamiClientBuilder {

  private final String serverUrl;

  public CudamiClientBuilder(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public CudamiClient build() {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiClient backend = ReflectiveFeign.builder()
        .decoder(new JacksonDecoder(mapper))
        .encoder(new JacksonEncoder(mapper))
        .errorDecoder(new CudamiRestErrorDecoder())
        .logger(new Slf4jLogger())
        .logLevel(Logger.Level.BASIC)
        .retryer(new Retryer.Default())
        .target(CudamiClient.class, serverUrl);
    return backend;
  }
}
