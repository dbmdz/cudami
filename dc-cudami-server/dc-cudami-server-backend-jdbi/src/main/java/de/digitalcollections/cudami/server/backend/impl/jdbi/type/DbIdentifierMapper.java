package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.identifiable.Identifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.array.SqlArrayType;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class DbIdentifierMapper implements SqlArrayType<Identifier>, ColumnMapper<Identifier> {

  @Override
  public String getTypeName() {
    return "dbIdentifier";
  }

  @Override
  public Object convertArrayElement(Identifier identifier) {
    return String.format("(\"%s\",\"%s\")", identifier.getNamespace(), identifier.getId());
  }

  @Override
  public Identifier map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
    String dbIdentifier = r.getString(columnNumber);
    Identifier identifier = extractIdentifier(dbIdentifier);
    return identifier;
  }

  protected Identifier extractIdentifier(String identifierString) {
    if (identifierString == null) {
      return null;
    }
    identifierString =
        identifierString
            .replaceFirst("^\\(", "")
            .replaceFirst("\\)$", ""); // Skip surrounding brackets
    if (identifierString.isBlank()) {
      return null;
    }

    // The identifierString can now contain quotation marks around one or both values, if they
    // contain commas or quotation marks
    // Otherwise, the values are NOT surrounded with quotation marks.
    //
    // Examples: ("name,space1",id1) and (namespace2,id2)
    //
    // To make things easier, we now ensure, that they are all surrounded by quotation marks
    if (!identifierString.startsWith("\"")) {
      // No quotation mark at the beginning
      // => we have to add one at the first position
      //    and the other right before the first(!) comma
      //    (because if there was a comma, we would already
      //     be surrounded by brackets)
      identifierString = identifierString.replaceFirst("^(.*?),", "\"$1\",");
    }
    if (!identifierString.endsWith("\"")) {
      // Same here. We have to put the quotation mark after
      // the last comma and at the end of the string
      identifierString = identifierString.replaceFirst("\",(.*)$", "\",\"$1\"");
    }

    // Now we can savely split at the ","
    String[] identifierParts = identifierString.split("\",\"");

    if (identifierParts.length != 2) {
      return null;
    }

    String namespace = identifierParts[0].replaceFirst("^\"", "");
    String id = identifierParts[1].replaceFirst("\"$", "");
    if (namespace == null || namespace.isBlank() || id == null || id.isBlank()) {
      return null;
    }
    return Identifier.builder().namespace(namespace).id(id).build();
  }
}
