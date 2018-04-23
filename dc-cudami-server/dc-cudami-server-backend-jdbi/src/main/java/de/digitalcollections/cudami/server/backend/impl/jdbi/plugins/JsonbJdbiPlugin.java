package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.jdbi.JsonbArgumentFactory;
import de.digitalcollections.commons.jdbi.JsonbColumnMapperFactory;
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.IiifImage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;

public class JsonbJdbiPlugin implements JdbiPlugin {

  private final ObjectMapper objectMapper;

  public JsonbJdbiPlugin(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void customizeJdbi(Jdbi db) {
    db.registerArgument(new JsonbArgumentFactory<>(IiifImage.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(MultilanguageDocument.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(Text.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(IiifImage.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(MultilanguageDocument.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(Text.class, objectMapper));
  }

}
