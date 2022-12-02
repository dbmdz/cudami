package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.MainSubType;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.manifestation.TitleType;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdbi.v3.core.array.SqlArrayType;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.util.StringUtils;

public class MainSubTypeMapper<M extends MainSubType> {

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

  public M createTypeFromString(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }

    // \p{Punct} is punctuation including " and \
    Matcher valueParts =
        Pattern.compile(
                "^\\p{Punct}{0,2}[(]\\p{Punct}{0,2}([\\w\\p{Punct}]*)\\p{Punct}{0,2},\\p{Punct}{0,2}([\\w\\p{Punct}]*)\\p{Punct}{0,2}[)]\\p{Punct}{0,2}$")
            .matcher(value);
    if (!valueParts.find()) {
      return null;
    }
    M result = null;
    try {
      result =
          derivedClass
              .getConstructor(String.class, String.class)
              .newInstance(valueParts.group(1), valueParts.group(2));
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return result;
  }

  public M map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
    String value = r.getString(columnNumber);
    return createTypeFromString(value);
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
