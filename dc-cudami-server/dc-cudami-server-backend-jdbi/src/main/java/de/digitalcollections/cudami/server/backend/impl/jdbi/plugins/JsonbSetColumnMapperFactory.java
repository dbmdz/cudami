package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonbSetColumnMapperFactory<T> implements ColumnMapperFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonbSetColumnMapperFactory.class);

  private final Class clz;
  private final ObjectMapper objectMapper;

  public JsonbSetColumnMapperFactory(Class<T> clz, ObjectMapper objectMapper) {
    this.clz = clz;
    this.objectMapper = objectMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
    if (!(type instanceof ParameterizedType)) {
      return Optional.empty();
    }

    Type setType = ((ParameterizedType) type).getActualTypeArguments()[0];
    if (!clz.equals(setType)) {
      return Optional.empty();
    }
    return Optional.of(
        (r, i, c) -> {
          String jsonb = r.getString(i);
          if (jsonb == null) {
            return null;
          }
          /* see https://stackoverflow.com/a/11681540 */
          try {
            JavaType javaType =
                objectMapper.getTypeFactory().constructParametricType(Set.class, (Class) setType);

            return objectMapper.readValue(jsonb, javaType);
          } catch (IOException ex) {
            LOGGER.error("IO error deserializing JSON: " + ex, ex);
            return null;
          } catch (Exception ex) {
            LOGGER.error("Error deserializing JSON: " + ex, ex);
            return null;
          }
        });
  }
}
