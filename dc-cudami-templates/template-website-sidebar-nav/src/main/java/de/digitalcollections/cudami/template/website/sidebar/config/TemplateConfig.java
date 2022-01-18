package de.digitalcollections.cudami.template.website.sidebar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "template")
@ConstructorBinding
public class TemplateConfig {

  private final String name;
  private final int navMaxLevel;

  public TemplateConfig(String name, int navMaxLevel) {
    this.name = name;
    this.navMaxLevel = navMaxLevel;
  }

  public String getName() {
    return name;
  }

  public int getNavMaxLevel() {
    return navMaxLevel;
  }
}
