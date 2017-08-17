package de.digitalcollections.cudami.server.backend.impl.jdbi.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Thumbnail;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonbColumnMapperFactory implements ColumnMapperFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonbColumnMapperFactory.class);

  private final ObjectMapper objectMapper;

  public JsonbColumnMapperFactory(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {

    if (type == Text.class || type == Thumbnail.class) {
      return Optional.of((r, i, c) -> {
        String jsonb = r.getString(i);

        try {
          Object obj = objectMapper.readValue(jsonb, (Class) type);
          return obj;
        } catch (IOException ex) {
          LOGGER.error("Error deserializing JSON", ex);
          return null;
        }
      });
    }
    return Optional.empty();
  }
}
