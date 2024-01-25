package de.digitalcollections.model.identifiable.alias;

import static de.digitalcollections.model.time.TimestampHelper.truncatedToMicros;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.entity.Website;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.experimental.SuperBuilder;

/**
 * A website and language specific "alias" (= human readable unique key) used as relative url part
 * for a website specific domain. The absolute URL references an Identifiable, e.g. a specific
 * webpage or collection.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class UrlAlias extends UniqueObject {

  public abstract static class UrlAliasBuilder<C extends UrlAlias, B extends UrlAliasBuilder<C, B>>
      extends UniqueObjectBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }

    public B isPrimary() {
      this.primary = true;
      return self();
    }

    public B lastPublished(String lastPublished) {
      this.lastPublished = LocalDateTime.parse(lastPublished);
      return self();
    }

    public B slug(String slug) {
      this.slug = slug;
      return self();
    }

    public B targetLanguage(Locale targetLanguage) {
      this.targetLanguage = targetLanguage;
      return self();
    }

    public B targetLanguage(String targetLanguage) {
      this.targetLanguage = Locale.forLanguageTag(targetLanguage);
      return self();
    }

    public B target(Identifiable identifiable) {
      this.target = identifiable;
      return self();
    }

    public B website(Website website) {
      this.website = website;
      return self();
    }
  }

  private LocalDateTime lastPublished;
  private boolean primary;
  private String slug;
  private Identifiable target;
  private Locale targetLanguage;

  private Website website;

  public UrlAlias() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UrlAlias)) {
      return false;
    }
    UrlAlias other = (UrlAlias) obj;
    return super.equals(obj)
        && Objects.equals(this.lastPublished, other.lastPublished)
        && this.primary == other.primary
        && Objects.equals(this.slug, other.slug)
        // && Objects.equals((this.target != null ? this.target.getUuid() : null),
        // (other.target != null ? other.target.getUuid() : null))
        && Objects.equals(this.targetLanguage, other.targetLanguage)
        && Objects.equals(this.uuid, other.uuid)
        && Objects.equals(
            (this.website != null ? this.website.getUuid() : null),
            (other.website != null ? other.website.getUuid() : null));
  }

  public LocalDateTime getLastPublished() {
    return this.lastPublished;
  }

  public String getSlug() {
    return this.slug;
  }

  public Identifiable getTarget() {
    return target;
  }

  @Deprecated(forRemoval = true)
  public IdentifiableObjectType getTargetIdentifiableObjectType() {
    if (target != null) {
      return target.getIdentifiableObjectType();
    }
    return null;
  }

  @Deprecated(forRemoval = true)
  public IdentifiableType getTargetIdentifiableType() {
    if (target != null) {
      return target.getType();
    }
    return null;
  }

  public Locale getTargetLanguage() {
    return this.targetLanguage;
  }

  @Deprecated(forRemoval = true)
  public UUID getTargetUuid() {
    if (target != null) {
      return target.getUuid();
    }
    return null;
  }

  public Website getWebsite() {
    return this.website;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.created,
        this.lastModified,
        this.lastPublished,
        this.primary,
        this.slug,
        this.targetLanguage,
        (this.target != null ? target.getUuid() : null),
        this.uuid,
        this.website);
  }

  @Override
  protected void init() {
    super.init();
  }

  public boolean isPrimary() {
    return this.primary;
  }

  public void setLastPublished(LocalDateTime lastPublished) {
    this.lastPublished = truncatedToMicros(lastPublished);
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public void setTarget(Identifiable target) {
    this.target = target;
  }

  public void setTargetLanguage(Locale targetLanguage) {
    this.targetLanguage = targetLanguage;
  }

  public void setWebsite(Website website) {
    this.website = website;
  }

  @Override
  public String toString() {
    return "UrlAlias{"
        + "created="
        + created
        + ", lastModified="
        + lastModified
        + ", lastPublished="
        + lastPublished
        + ", primary="
        + primary
        + ", slug='"
        + slug
        + '\''
        + ", target="
        + (target != null ? (target.getUuid() != null ? target.getUuid() : "????") : null)
        + ", targetLanguage="
        + targetLanguage
        + ", uuid="
        + uuid
        + ", website="
        + website
        + '}';
  }
}
