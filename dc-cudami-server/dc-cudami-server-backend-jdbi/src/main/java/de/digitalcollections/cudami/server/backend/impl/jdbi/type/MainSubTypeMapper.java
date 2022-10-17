package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.MainSubType;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.identifiable.entity.work.TitleType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.array.SqlArrayType;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class MainSubTypeMapper<M extends MainSubType> {

  @SuppressFBWarnings("URF_UNREAD_FIELD")
  private Class<M> derivedClass;

  protected MainSubTypeMapper(Class<M> derivedClass) {
    this.derivedClass = derivedClass;
  }

  public String getTypeName() {
    return "mainsubtype";
  }

  public Object convertArrayElement(M element) {
    return "("
        + (element.getMainType() != null
            ? String.format("\"%s\"", element.getMainType().replaceAll("['\"]", "$0$0"))
            : "")
        + (element.getSubType() != null
            ? String.format(",\"%s\"", element.getSubType().replaceAll("['\"]", "$0$0"))
            : ",")
        + ")";
  }

  public M map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public static class ExpressionTypeMapper extends MainSubTypeMapper<ExpressionType>
      implements SqlArrayType<ExpressionType>, ColumnMapper<ExpressionType> {

    public ExpressionTypeMapper() {
      super(ExpressionType.class);
    }
  }

  public static class TitleTypeMapper extends MainSubTypeMapper<TitleType>
      implements SqlArrayType<TitleType>, ColumnMapper<TitleType> {

    public TitleTypeMapper() {
      super(TitleType.class);
    }
  }
}
