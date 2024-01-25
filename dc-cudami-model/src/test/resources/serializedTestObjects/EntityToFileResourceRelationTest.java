package de.digitalcollections.model.jackson.identifiable.entity.relation;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.relation.EntityToEntityRelation;
import de.digitalcollections.model.identifiable.entity.relation.EntityToFileResourceRelation;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;

public class EntityToFileResourceRelationTest extends BaseJsonSerializationTest {

  private EntityToFileResourceRelation createObject() {
    EntityToFileResourceRelation relation = new EntityToFileResourceRelation();
    Article subjectEntity = new Article();
    subjectEntity.setUuid(UUID.fromString("8a9c3c34-c36c-4671-8f2f-9d96a5fc32e4"));
    relation.setSubject(subjectEntity);
    relation.setPredicate("has_attachment");
    
    ApplicationFileResource object = new ApplicationFileResource();
    object.setUuid(UUID.fromString("baf5a649-dd8a-43f2-8fac-f535b311af03"));
    relation.setObject(object);
    relation.setAdditionalPredicates(List.of("is_legal_stuff"));
    return relation;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    EntityToFileResourceRelation relation = createObject();
    checkSerializeDeserialize(
        relation,
        "serializedTestObjects/identifiable/entity/relation/EntityToEntityRelation.json");
  }
}
