package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.jdbi.JsonbArgumentFactory;
import de.digitalcollections.commons.jdbi.JsonbColumnMapperFactory;
import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import de.digitalcollections.model.api.identifiable.parts.structuredcontent.LocalizedStructuredContent;
import de.digitalcollections.model.api.identifiable.resource.IiifImage;
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
    db.registerArgument(new JsonbArgumentFactory<>(LocalizedStructuredContent.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(LocalizedText.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(IiifImage.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(LocalizedStructuredContent.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(LocalizedText.class, objectMapper));
  }

}
