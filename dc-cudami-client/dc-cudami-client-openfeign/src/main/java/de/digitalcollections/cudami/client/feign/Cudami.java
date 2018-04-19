package de.digitalcollections.cudami.client.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.feign.api.Client;
import de.digitalcollections.cudami.client.feign.api.CudamiClient;
import de.digitalcollections.cudami.client.feign.config.BackendUrls;
import de.digitalcollections.cudami.client.feign.config.BackendUrlsFromConfig;
import de.digitalcollections.cudami.client.feign.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.feign.impl.ClientFactory;
import de.digitalcollections.cudami.model.jackson.CudamiModule;
import de.digitalcollections.prosemirror.model.jackson.ProseMirrorModule;
import feign.Feign;
import feign.Logger;
import feign.ReflectiveFeign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.lang.invoke.MethodHandles;
import java.net.URI;
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

  public Cudami(URI uri) {
    this(uri.toString());
  }

  public Cudami(String url) {
    this(new BackendUrls() {
      @Override
      public String forBackend(String backend) {
        return url;
      }
    });
  }

  public Cudami(BackendUrls backendUrls) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new CudamiModule());
    mapper.registerModule(new ProseMirrorModule());

    Feign.Builder feign = ReflectiveFeign.builder()
        .decoder(new JacksonDecoder(mapper))
        .encoder(new JacksonEncoder(mapper))
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
