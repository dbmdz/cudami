package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.jdbi.JsonbArgumentFactory;
import de.digitalcollections.commons.jdbi.JsonbColumnMapperFactory;
import de.digitalcollections.model.api.identifiable.entity.parts.CustomAttributes;
import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import de.digitalcollections.model.api.identifiable.parts.RenderingHintsPreviewImage;
import de.digitalcollections.model.api.identifiable.parts.structuredcontent.LocalizedStructuredContent;
import de.digitalcollections.model.impl.view.RenderingHints;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;

public class JsonbJdbiPlugin implements JdbiPlugin {

  private final ObjectMapper objectMapper;

  public JsonbJdbiPlugin(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void customizeJdbi(Jdbi db) {
    // argument factories
    db.registerArgument(new JsonbArgumentFactory<>(CustomAttributes.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(LocalizedStructuredContent.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(LocalizedText.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(RenderingHints.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(RenderingHintsPreviewImage.class, objectMapper));

    // column mapper
    db.registerColumnMapper(new JsonbColumnMapperFactory(CustomAttributes.class, objectMapper));
    db.registerColumnMapper(
        new JsonbColumnMapperFactory(LocalizedStructuredContent.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(LocalizedText.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(RenderingHints.class, objectMapper));
    db.registerColumnMapper(
        new JsonbColumnMapperFactory(RenderingHintsPreviewImage.class, objectMapper));
  }
}
