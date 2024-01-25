package de.digitalcollections.model.identifiable.agent;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Set;
import lombok.experimental.SuperBuilder;

/**
 * see Wikidata https://www.wikidata.org/wiki/Q101352: "Part of a naming scheme for individuals,
 * used in many cultures worldwide."
 */
@SuperBuilder(buildMethodName = "prebuild")
public class FamilyName extends Identifiable {

  public FamilyName() {
    super();
  }

  public FamilyName(LocalizedText label, Set<Identifier> identifiers) {
    this();
    this.label = label;
    getIdentifiers().addAll(identifiers);
  }

  @Override
  protected void init() {
    super.init();
    this.type = IdentifiableType.RESOURCE;
  }

  @Override
  public String toString() {
    return "FamilyName{"
        + "description="
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

  public abstract static class FamilyNameBuilder<
          C extends FamilyName, B extends FamilyNameBuilder<C, B>>
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
