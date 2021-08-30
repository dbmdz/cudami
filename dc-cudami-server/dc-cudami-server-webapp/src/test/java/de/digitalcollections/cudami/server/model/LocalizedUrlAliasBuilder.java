package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;

public class LocalizedUrlAliasBuilder {

  LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases();

  public LocalizedUrlAliases build() {
    return localizedUrlAliases;
  }

  public LocalizedUrlAliasBuilder addUrlAlias(UrlAlias urlAlias) {
    localizedUrlAliases.add(urlAlias);
    return this;
  }
}
