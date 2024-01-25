package de.digitalcollections.model.jackson.identifiable.relation;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.relation.IdentifiableToEntityRelation;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The IdentifiableToEntityRelation")
public class IdentifiableToEntityRelationTest extends BaseJsonSerializationTest {

  private IdentifiableToEntityRelation createObject() {
    IdentifiableToEntityRelation relation = new IdentifiableToEntityRelation();
    Identifiable subject = new Identifiable();
    subject.setUuid(UUID.fromString("8a9c3c34-c36c-4671-8f2f-9d96a5fc32e4"));
    relation.setSubject(subject);
    relation.setPredicate("is_digital_represented_by");
    DigitalObject object = new DigitalObject();
    object.setUuid(UUID.fromString("baf5a649-dd8a-43f2-8fac-f535b311af03"));
    relation.setObject(object);
    relation.setAdditionalPredicates(List.of("another_relation"));
    return relation;
  }

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    IdentifiableToEntityRelation entityRelation = createObject();
    checkSerializeDeserialize(
        entityRelation,
        "serializedTestObjects/identifiable/relation/IdentifiableToEntityRelation.json");
  }
}
