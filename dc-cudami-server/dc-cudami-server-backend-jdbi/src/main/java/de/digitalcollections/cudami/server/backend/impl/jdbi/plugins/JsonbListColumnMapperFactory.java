package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonbListColumnMapperFactory<T> implements ColumnMapperFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonbListColumnMapperFactory.class);

  private final Class clz;
  private final ObjectMapper objectMapper;

  public JsonbListColumnMapperFactory(Class<T> clz, ObjectMapper objectMapper) {
    this.clz = clz;
    this.objectMapper = objectMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
    if (!(type instanceof ParameterizedType)) {
      return Optional.empty();
    }

    Type listType = ((ParameterizedType) type).getActualTypeArguments()[0];
    if (!clz.equals(listType)) {
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
                objectMapper.getTypeFactory().constructParametricType(List.class, (Class) listType);

            return objectMapper.readValue(jsonb, javaType);
          } catch (IOException ex) {
            ex.printStackTrace();
            LOGGER.error("Error deserializing JSON", ex);
            return null;
          } catch (Exception e) {
            e.printStackTrace();
            return null;
          }
        });
  }
}
