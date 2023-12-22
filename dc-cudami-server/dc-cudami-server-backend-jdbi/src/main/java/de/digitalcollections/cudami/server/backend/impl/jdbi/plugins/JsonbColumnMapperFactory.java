package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonbColumnMapperFactory<T> implements ColumnMapperFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonbColumnMapperFactory.class);

  private final Class clz;
  private final ObjectMapper objectMapper;

  public JsonbColumnMapperFactory(Class<T> clz, ObjectMapper objectMapper) {
    this.clz = clz;
    this.objectMapper = objectMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
    if (!clz.equals(type)) {
      return Optional.empty();
    }
    return Optional.of(
        (r, i, c) -> {
          String jsonb = r.getString(i);
          if (jsonb == null) {
            return null;
          }
          try {
            return objectMapper.readValue(jsonb, (Class) type);
          } catch (IOException ex) {
            LOGGER.error("Error deserializing JSON", ex);
            return null;
          }
        });
  }
}
