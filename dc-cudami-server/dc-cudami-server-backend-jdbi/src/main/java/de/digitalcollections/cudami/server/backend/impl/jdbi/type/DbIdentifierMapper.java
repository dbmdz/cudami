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
    return "('" + identifier.getNamespace() + "','" + identifier.getId() + "')";
  }

  @Override
  public Identifier map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
    String dbIdentifier = r.getString(columnNumber);
    return extractIdentifier(dbIdentifier);
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

    String[] identifierParts = identifierString.split("','");

    if (identifierParts.length != 2) {
      return null;
    }

    String namespace = identifierParts[0].replaceFirst("^'", "");
    String id = identifierParts[1].replaceFirst("'$", "");
    if (namespace == null || namespace.isBlank() || id == null || id.isBlank()) {
      return null;
    }
    return Identifier.builder().namespace(namespace).id(id).build();
  }
}
