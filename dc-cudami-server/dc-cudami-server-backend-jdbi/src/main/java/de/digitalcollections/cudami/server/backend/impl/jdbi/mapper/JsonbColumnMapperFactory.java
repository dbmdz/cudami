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

public class JsonbColumnMapperFactory implements ColumnMapperFactory {

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
          return objectMapper.readValue(jsonb, type.getClass());
        } catch (IOException ex) {
          return null;
        }
      });
    }
    return Optional.empty();
  }
}
