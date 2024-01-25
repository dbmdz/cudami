package de.digitalcollections.model.identifiable;

import de.digitalcollections.model.UniqueObject;
import lombok.experimental.SuperBuilder;

/**
 * Defines an identifier type to be used in the system implementing this library. It is up to the
 * implementor to add all relevant identifier types to his system. Each IdentifierType can be
 * described by a label, a namespace (describing the system) and a regex pattern for validating
 * identifiers.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class IdentifierType extends UniqueObject {
  private String label;
  private String namespace;
  private String pattern;

  public IdentifierType() {
    super();
  }

  public String getLabel() {
    return label;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getPattern() {
    return pattern;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
