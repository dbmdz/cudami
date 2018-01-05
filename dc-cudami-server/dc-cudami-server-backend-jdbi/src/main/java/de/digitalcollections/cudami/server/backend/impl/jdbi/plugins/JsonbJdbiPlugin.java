package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.mapper.JsonbColumnMapperFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;

public class JsonbJdbiPlugin implements JdbiPlugin {

  private final ObjectMapper objectMapper;

  public JsonbJdbiPlugin(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void customizeJdbi(Jdbi db) {
    db.registerColumnMapper(new JsonbColumnMapperFactory(objectMapper));
  }

}
