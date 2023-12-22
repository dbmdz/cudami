package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;

public class LocaleColumnMapperFactory implements ColumnMapperFactory {

  @Override
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
    if (type == Locale.class) {
      return Optional.ofNullable(
          (resultSet, columnNumber, statementContext) -> {
            final String localeString = resultSet.getString(columnNumber);
            if (localeString == null) {
              return null;
            }
            return Locale.forLanguageTag(localeString);
          });
    }
    return Optional.empty();
  }
}
