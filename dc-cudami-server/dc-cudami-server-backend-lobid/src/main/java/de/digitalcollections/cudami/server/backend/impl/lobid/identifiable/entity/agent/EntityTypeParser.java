package de.digitalcollections.cudami.server.backend.impl.lobid.identifiable.entity.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.digitalcollections.model.identifiable.entity.EntityType;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityTypeParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityTypeParser.class);

  public static EntityType parse(JsonNode rootNode) throws JsonProcessingException {
    Iterator<JsonNode> types = rootNode.get("type").elements();
    while (types.hasNext()) {
      JsonNode type = types.next();
      LOGGER.info("type: {}", type);
      switch (type.asText()) {
        case "CorporateBody":
          return EntityType.CORPORATE_BODY;
        case "DifferentiatedPerson":
          return EntityType.PERSON;
        case "Person":
          return EntityType.PERSON;
        case "Work":
          return EntityType.WORK;
        default:
      }
    }
    return null;
  }

  private EntityTypeParser() {}
}
