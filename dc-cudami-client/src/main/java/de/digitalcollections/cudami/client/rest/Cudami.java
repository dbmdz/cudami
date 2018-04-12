package de.digitalcollections.cudami.client.rest;

import de.digitalcollections.cudami.client.rest.api.Client;
import de.digitalcollections.cudami.client.rest.api.CudamiClient;
import de.digitalcollections.cudami.client.rest.config.BackendUrls;
import de.digitalcollections.cudami.client.rest.config.BackendUrlsFromConfig;
import de.digitalcollections.cudami.client.rest.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.rest.impl.ClientFactory;
import de.digitalcollections.cudami.model.jackson.CudamiObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.ReflectiveFeign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.LoggerFactory;

public class Cudami {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ClientFactory clientFactory;

  private final ConcurrentMap<Class, Client> clients;

  public Cudami(Environment environment) {
    this(new BackendUrlsFromConfig(environment));
  }

  public Cudami(BackendUrls backendUrls) {
    final CudamiObjectMapper objectMapper = new CudamiObjectMapper();

    Feign.Builder feign = ReflectiveFeign.builder()
        .decoder(new JacksonDecoder(objectMapper))
        .encoder(new JacksonEncoder(objectMapper))
        .errorDecoder(new CudamiRestErrorDecoder())
        .logger(new Slf4jLogger())
        .logLevel(Logger.Level.BASIC)
        .retryer(new Retryer.Default());
    this.clientFactory = new ClientFactory(feign, backendUrls);
    this.clients = new ConcurrentHashMap();
  }

  public CudamiClient cudamiClient() {
    return (CudamiClient) clients.computeIfAbsent(CudamiClient.class, __ -> clientFactory.createCudamiClient());
  }

}
