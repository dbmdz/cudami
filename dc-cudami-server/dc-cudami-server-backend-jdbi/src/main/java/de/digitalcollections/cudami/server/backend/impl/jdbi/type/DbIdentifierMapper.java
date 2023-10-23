package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.identifiable.Identifier;
import org.jdbi.v3.core.array.SqlArrayType;

public class DbIdentifierMapper implements SqlArrayType<Identifier> {

  @Override
  public String getTypeName() {
    return "dbIdentifier";
  }

  @Override
  public Object convertArrayElement(Identifier identifier) {
    return String.format("(\"%s\",\"%s\")", identifier.getNamespace(), identifier.getId());
  }
}
