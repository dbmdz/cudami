package de.digitalcollections.model.identifiable.agent;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.SuperBuilder;

/**
 * see WikiData https://www.wikidata.org/wiki/Q202444: "Name typically used to differentiate people
 * from the same family, clan, or other social group who have a common last name."
 */
@SuperBuilder(buildMethodName = "prebuild")
public class GivenName extends Identifiable {

  private Gender gender;

  public GivenName() {
    super();
  }

  public GivenName(Gender gender, LocalizedText label, Set<Identifier> identifiers) {
    this();
    this.gender = gender;
    this.label = label;
    getIdentifiers().addAll(identifiers);
  }

  public Gender getGender() {
    return gender;
  }

  @Override
  protected void init() {
    super.init();
    this.type = IdentifiableType.RESOURCE;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public static enum Gender {
    MALE, // see https://www.wikidata.org/wiki/Q12308941
    FEMALE, // see https://www.wikidata.org/wiki/Q11879590
    UNISEX; // see https://www.wikidata.org/wiki/Q3409032
  }

  @Override
  public String toString() {
    return "GivenName{"
        + "gender="
        + gender
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
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GivenName)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    GivenName givenName = (GivenName) o;
    return gender == givenName.gender;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), gender);
  }

  public abstract static class GivenNameBuilder<
          C extends GivenName, B extends GivenNameBuilder<C, B>>
      extends IdentifiableBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }
}
