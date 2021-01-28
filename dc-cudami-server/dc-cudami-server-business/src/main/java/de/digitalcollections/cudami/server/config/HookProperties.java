package de.digitalcollections.cudami.server.config;

import de.digitalcollections.model.api.identifiable.entity.enums.EntityType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class HookProperties {

  private Map<String, String> hooks = new HashMap<>();

  public Optional<String> getHookForActionAndType(String action, EntityType type) {
    String key = String.format("%s-%s", action, type.toString().toLowerCase());
    return Optional.ofNullable(hooks.get(key));
  }

  public void setHooks(Map<String, String> hooks) {
    this.hooks = hooks;
  }
}
