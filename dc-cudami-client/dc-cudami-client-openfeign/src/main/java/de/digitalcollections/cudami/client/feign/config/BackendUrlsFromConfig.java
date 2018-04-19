package de.digitalcollections.cudami.client.feign.config;

import de.digitalcollections.cudami.client.feign.Environment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class BackendUrlsFromConfig implements BackendUrls {


  private static final String BACKENDS = "/cudami-backends.yml";

  private final Map<String, Map<String, String>> backendMapping;

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendUrlsFromConfig.class);

  private final Environment environment;

  public BackendUrlsFromConfig(Environment environment) {
    this.environment = environment;
    this.backendMapping = loadBackendMapping();

  }

  @Override
  public String forBackend(String backend) {
    Map<String, String> urls = backendMapping.get(environment.name().toLowerCase());
    if (urls == null) {
      throw new IllegalStateException("No mapping for environment " + environment + " found.");
    }
    return urls.getOrDefault(backend, urls.get("default"));
  }

  private Map<String, Map<String, String>> loadBackendMapping() {
    LOGGER.info("Loading Cudami backend urls for environment " + environment + ".");
    Yaml yaml = new Yaml();
    Map<String, Map<String, String>> result = null;
    try (InputStream in = BackendUrlsFromConfig.class.getResourceAsStream(BACKENDS)) {
      result = (Map<String, Map<String, String>>) yaml.load(in);
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
    if (result == null) {
      throw new IllegalStateException("Could not load backend mapping for '" + BACKENDS + "'");
    }
    return result;
  }


}
