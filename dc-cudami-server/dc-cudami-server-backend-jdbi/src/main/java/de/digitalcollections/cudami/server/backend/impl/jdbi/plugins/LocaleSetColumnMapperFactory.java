package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import java.lang.reflect.Type;
import java.sql.Array;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;

public class LocaleSetColumnMapperFactory implements ColumnMapperFactory {

  @Override
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
    if (type == Set.class) {
      return Optional.ofNullable(
          (resultSet, columnNumber, statementContext) -> {
            final Array localeArray = resultSet.getArray(columnNumber);
            if (localeArray == null) {
              return null;
            }
            return Arrays.stream((String[]) localeArray.getArray())
                .map(Locale::forLanguageTag)
                .collect(Collectors.toSet());
          });
    }
    return Optional.empty();
  }
}
