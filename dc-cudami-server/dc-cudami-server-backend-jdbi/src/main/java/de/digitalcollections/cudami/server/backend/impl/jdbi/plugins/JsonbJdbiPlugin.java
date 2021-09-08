package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.jdbi.JsonbArgumentFactory;
import de.digitalcollections.commons.jdbi.JsonbColumnMapperFactory;
import de.digitalcollections.commons.jdbi.LocaleArgumentFactory;
import de.digitalcollections.commons.jdbi.LocaleColumnMapperFactory;
import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.entity.CustomAttributes;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingHints;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

public class JsonbJdbiPlugin implements JdbiPlugin {

  private final ObjectMapper objectMapper;

  public JsonbJdbiPlugin(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void customizeJdbi(Jdbi db) {
    // argument factories
    db.registerArgument(new JsonbArgumentFactory<>(CoordinateLocation.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(CustomAttributes.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(LocalizedStructuredContent.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(LocalizedText.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(RenderingHints.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(RenderingHintsPreviewImage.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(TimeValue.class, objectMapper));
    db.registerArgument(new LocaleArgumentFactory());

    // column mapper
    db.registerColumnMapper(new JsonbColumnMapperFactory(CoordinateLocation.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(CustomAttributes.class, objectMapper));
    db.registerColumnMapper(
        new JsonbColumnMapperFactory(LocalizedStructuredContent.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(LocalizedText.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(RenderingHints.class, objectMapper));
    db.registerColumnMapper(
        new JsonbColumnMapperFactory(RenderingHintsPreviewImage.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(TimeValue.class, objectMapper));
    db.registerColumnMapper(new LocaleColumnMapperFactory());
  }
}
