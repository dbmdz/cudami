package de.digitalcollections.cudami.server.backend.impl.model;

import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TestModelFixture {

  public static DigitalObject createDigitalObject(
      Map<Locale, String> labelMap, Map<Locale, String> descriptionMap) {
    DigitalObject digitalObject = new DigitalObject();
    LocalizedText labelText = new LocalizedText();
    for (Map.Entry<Locale, String> entry : labelMap.entrySet()) {
      labelText.setText(entry.getKey(), entry.getValue());
    }
    digitalObject.setLabel(labelText);

    LocalizedStructuredContent descriptionContent = new LocalizedStructuredContent();
    for (Map.Entry<Locale, String> entry : descriptionMap.entrySet()) {
      StructuredContent structuredContent = new StructuredContent();
      Paragraph paragraph = new Paragraph(entry.getValue());
      structuredContent.addContentBlock(paragraph);
      descriptionContent.put(entry.getKey(), structuredContent);
    }
    digitalObject.setDescription(descriptionContent);

    return digitalObject;
  }

  public static EntityRelation createEntityRelation(
      Map<Locale, String> subjectLabels, Map<Locale, String> objectLabels, String predicate) {
    Entity subject = createDigitalObject(subjectLabels, Map.of());
    subject.setUuid(UUID.randomUUID());
    Entity object = createDigitalObject(objectLabels, Map.of());
    object.setUuid(UUID.randomUUID());
    return new EntityRelation(subject, predicate, object);
  }
}
