package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class JsonbListArgumentFactory<T> implements ArgumentFactory {

  private final Class<T> clz;
  private final ObjectMapper objectMapper;

  public JsonbListArgumentFactory(Class<T> clz, ObjectMapper objectMapper) {
    this.clz = clz;
    this.objectMapper = objectMapper;
  }

  protected Argument build(List<T> value, ConfigRegistry config) {
    return (i, p, cx) -> {
      if (value == null) {
        p.setNull(i, Types.OTHER);
      } else {
        try {
          p.setString(i, objectMapper.writeValueAsString(value));
        } catch (IOException ex) {
          throw new SQLException(ex);
        }
      }
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
    if (!(type instanceof ParameterizedType)) {
      return Optional.empty();
    }

    Type listType = ((ParameterizedType) type).getActualTypeArguments()[0];

    if (!clz.equals(listType)) {
      return Optional.empty();
    }
    return Optional.of(
        value == null ? new NullArgument(Types.OTHER) : build((List<T>) value, config));
  }
}
