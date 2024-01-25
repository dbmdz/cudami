package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;

public class UrlColumnMapperFactory implements ColumnMapperFactory {

  @Override
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
    if (type == URL.class) {
      return Optional.of(
          (r, i, c) -> {
            try {
              return new URL(r.getString(i));
            } catch (MalformedURLException ex) {
              return null;
            }
          });
    }
    return Optional.empty();
  }
}
