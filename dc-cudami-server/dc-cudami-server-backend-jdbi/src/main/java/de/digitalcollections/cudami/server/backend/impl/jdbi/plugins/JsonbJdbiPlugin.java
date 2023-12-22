package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.CustomAttributes;
import de.digitalcollections.model.identifiable.entity.manifestation.DistributionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.ProductionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.time.TimeValueRange;
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
    db.registerArgument(
        new JsonbListArgumentFactory<>(LocalizedStructuredContent.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(TimeValueRange.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(PublicationInfo.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(ProductionInfo.class, objectMapper));
    db.registerArgument(new JsonbArgumentFactory<>(DistributionInfo.class, objectMapper));

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
    db.registerColumnMapper(
        new JsonbListColumnMapperFactory(LocalizedStructuredContent.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(TimeValueRange.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(PublicationInfo.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(ProductionInfo.class, objectMapper));
    db.registerColumnMapper(new JsonbColumnMapperFactory(DistributionInfo.class, objectMapper));
    db.registerColumnMapper(new JsonbSetColumnMapperFactory(Identifier.class, objectMapper));
  }
}
