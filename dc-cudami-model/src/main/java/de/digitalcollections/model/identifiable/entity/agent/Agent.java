package de.digitalcollections.model.identifiable.entity.agent;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.NamedEntity;
import de.digitalcollections.model.text.LocalizedText;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.SuperBuilder;

/**
 * https://books.google.de/books?id=foGBCgAAQBAJ&amp;pg=PA151:
 *
 * <p>The classes corresponding to the entities person, family and corporate body are subclasses of
 * the Agent class, since these entities share attributes and relationships between them. For
 * instance, intellectual creations may be gathered into a class "Work" and its creators may be
 * gathered into classes like "Person", "Family" and "Corporate body".
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Agent extends Entity implements NamedEntity {

  protected LocalizedText name;
  protected Set<Locale> nameLocalesOfOriginalScripts;

  public Agent() {
    super();
  }

  @Override
  protected void init() {
    super.init();
    if (nameLocalesOfOriginalScripts == null) nameLocalesOfOriginalScripts = new HashSet<>(0);
  }

  @Override
  public LocalizedText getName() {
    return name;
  }

  @Override
  public Set<Locale> getNameLocalesOfOriginalScripts() {
    return nameLocalesOfOriginalScripts;
  }

  @Override
  public void setName(LocalizedText name) {
    this.name = name;
  }

  @Override
  public void setNameLocalesOfOriginalScripts(Set<Locale> localesOfOriginalScripts) {
    this.nameLocalesOfOriginalScripts = localesOfOriginalScripts;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Agent)) {
      return false;
    }
    Agent other = (Agent) obj;
    return obj == this
        || super.equals(obj)
            && Objects.equals(name, other.name)
            && Objects.equals(nameLocalesOfOriginalScripts, other.nameLocalesOfOriginalScripts);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + Objects.hash(name, nameLocalesOfOriginalScripts) + 93;
  }

  @Override
  public String toString() {
    return "Agent [name="
        + name
        + ", nameLocalesOfOriginalScripts="
        + nameLocalesOfOriginalScripts
        + ", customAttributes="
        + customAttributes
        + ", navDate="
        + navDate
        + ", refId="
        + refId
        + ", notes="
        + notes
        + ", description="
        + description
        + ", identifiableObjectType="
        + identifiableObjectType
        + ", identifiers="
        + identifiers
        + ", label="
        + label
        + ", localizedUrlAliases="
        + localizedUrlAliases
        + ", previewImage="
        + previewImage
        + ", previewImageRenderingHints="
        + previewImageRenderingHints
        + ", tags="
        + tags
        + ", type="
        + type
        + ", created="
        + created
        + ", lastModified="
        + lastModified
        + ", uuid="
        + uuid
        + ", name="
        + name
        + ", nameLocalesOfOriginalScripts="
        + nameLocalesOfOriginalScripts
        + "]";
  }

  public abstract static class AgentBuilder<C extends Agent, B extends AgentBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }

    public B addName(Locale locale, String name) {
      if (this.name == null) {
        this.name = new LocalizedText(locale, name);
      } else {
        this.name.put(locale, name);
      }
      return self();
    }

    public B addName(String name) {
      if (this.name == null) {
        this.name = new LocalizedText(Locale.ROOT, name);
      } else {
        this.name.put(Locale.ROOT, name);
      }
      return self();
    }
  }
}
