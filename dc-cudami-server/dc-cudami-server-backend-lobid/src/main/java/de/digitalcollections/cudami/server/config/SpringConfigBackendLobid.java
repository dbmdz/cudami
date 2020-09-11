package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.lobid.client.LobidClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"de.digitalcollections.cudami.server.backend.impl.lobid"})
public class SpringConfigBackendLobid {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackendLobid.class);

  @Bean
  public LobidClient lobidClient() {
    return new LobidClient();
  }
}
