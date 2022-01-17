package de.digitalcollections.cudami.template.website.sidebar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "template")
@ConstructorBinding
public class TemplateConfig {

  private final int navMaxLevel;

  public TemplateConfig(int navMaxLevel) {
    this.navMaxLevel = navMaxLevel;
  }

  public int getNavMaxLevel() {
    return navMaxLevel;
  }
}
