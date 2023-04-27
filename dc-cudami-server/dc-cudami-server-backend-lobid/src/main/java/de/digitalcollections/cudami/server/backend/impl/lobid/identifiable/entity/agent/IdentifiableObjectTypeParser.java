package de.digitalcollections.cudami.server.backend.impl.lobid.identifiable.entity.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifiableObjectTypeParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableObjectTypeParser.class);

  public static IdentifiableObjectType parse(JsonNode rootNode) throws JsonProcessingException {
    Iterator<JsonNode> types = rootNode.get("type").elements();
    while (types.hasNext()) {
      JsonNode type = types.next();
      LOGGER.info("type: {}", type);
      switch (type.asText()) {
        case "CorporateBody":
          return IdentifiableObjectType.CORPORATE_BODY;
        case "DifferentiatedPerson":
          return IdentifiableObjectType.PERSON;
        case "Person":
          return IdentifiableObjectType.PERSON;
        case "Work":
          return IdentifiableObjectType.WORK;
        default:
      }
    }
    return null;
  }

  private IdentifiableObjectTypeParser() {}
}
