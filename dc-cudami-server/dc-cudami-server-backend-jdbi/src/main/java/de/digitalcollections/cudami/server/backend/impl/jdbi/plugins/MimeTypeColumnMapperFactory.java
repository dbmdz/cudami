package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import de.digitalcollections.model.file.MimeType;
import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;

public class MimeTypeColumnMapperFactory implements ColumnMapperFactory {

  @Override
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
    if (type == MimeType.class) {
      return Optional.of(
          (resultSet, columnNumber, statementContext) -> {
            final String typename = resultSet.getString(columnNumber);
            if (typename == null) {
              return null;
            }
            return MimeType.fromTypename(typename);
          });
    }
    return Optional.empty();
  }
}
