package de.digitalcollections.cudami.admin.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/** Model for the webjar versions in <code>application.yml</code>. */
@ConfigurationProperties(prefix = "webjars")
@ConstructorBinding
public class WebjarProperties {

  private final Map<String, String> versions;

  public WebjarProperties(Map<String, String> versions) {
    this.versions = versions;
  }

  public Map<String, String> getVersions() {
    return versions;
  }
}
