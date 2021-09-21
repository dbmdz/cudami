package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public class UrlAliasBuilder {

  UrlAlias urlAlias = new UrlAlias();

  public UrlAlias build() {
    return urlAlias;
  }

  public UrlAliasBuilder createdAt(String createdAt) {
    urlAlias.setCreated(LocalDateTime.parse(createdAt));
    return this;
  }

  public UrlAliasBuilder lastPublishedAt(String lastPublishedAt) {
    urlAlias.setLastPublished(LocalDateTime.parse(lastPublishedAt));
    return this;
  }

  public UrlAliasBuilder isPrimary() {
    urlAlias.setPrimary(true);
    return this;
  }

  public UrlAliasBuilder withSlug(String slug) {
    urlAlias.setSlug(slug);
    return this;
  }

  public UrlAliasBuilder withTargetLanguage(String targetLanguage) {
    urlAlias.setTargetLanguage(Locale.forLanguageTag(targetLanguage));
    return this;
  }

  public UrlAliasBuilder withTargetType(IdentifiableType identifiableType, EntityType entityType) {
    urlAlias.setTargetIdentifiableType(identifiableType);
    urlAlias.setTargetEntityType(entityType);
    return this;
  }

  public UrlAliasBuilder withTargetUuid(String targetUuid) {
    urlAlias.setTargetUuid(UUID.fromString(targetUuid));
    return this;
  }

  public UrlAliasBuilder withUuid(String uuid) {
    urlAlias.setUuid(UUID.fromString(uuid));
    return this;
  }

  public UrlAliasBuilder withWebsite(Website website) {
    urlAlias.setWebsite(website);
    return this;
  }
}
