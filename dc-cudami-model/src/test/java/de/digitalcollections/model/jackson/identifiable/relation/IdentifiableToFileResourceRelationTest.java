package de.digitalcollections.model.jackson.identifiable.relation;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.relation.IdentifiableToFileResourceRelation;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The IdentifiableToFileResourceRelation")
public class IdentifiableToFileResourceRelationTest extends BaseJsonSerializationTest {

  private IdentifiableToFileResourceRelation createObject() {
    IdentifiableToFileResourceRelation relation = new IdentifiableToFileResourceRelation();
    Identifiable subject = new Identifiable();
    subject.setUuid(UUID.fromString("8a9c3c34-c36c-4671-8f2f-9d96a5fc32e4"));
    relation.setSubject(subject);
    relation.setPredicate("has_attachment");

    ApplicationFileResource object = new ApplicationFileResource();
    object.setUuid(UUID.fromString("baf5a649-dd8a-43f2-8fac-f535b311af03"));
    relation.setObject(object);
    relation.setAdditionalPredicates(List.of("is_legal_stuff"));
    return relation;
  }

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    IdentifiableToFileResourceRelation relation = createObject();
    checkSerializeDeserialize(
        relation,
        "serializedTestObjects/identifiable/relation/IdentifiableToFileResourceRelation.json");
  }
}
