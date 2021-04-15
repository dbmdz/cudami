package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.IdentifierType;
import java.util.UUID;

public class IdentifierTypeBuilder {

  public IdentifierType identifierType = new IdentifierType();

  public IdentifierType build() {
    return identifierType;
  }

  public IdentifierTypeBuilder withLabel(String label) {
    identifierType.setLabel(label);
    return this;
  }

  public IdentifierTypeBuilder withNamespace(String namespace) {
    identifierType.setNamespace(namespace);
    return this;
  }

  public IdentifierTypeBuilder withPattern(String pattern) {
    identifierType.setPattern(pattern);
    return this;
  }

  public IdentifierTypeBuilder withUuid(String uuid) {
    identifierType.setUuid(UUID.fromString(uuid));
    return this;
  }
}
