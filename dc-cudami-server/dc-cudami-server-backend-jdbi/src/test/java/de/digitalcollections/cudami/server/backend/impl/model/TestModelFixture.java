package de.digitalcollections.cudami.server.backend.impl.model;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TestModelFixture {

  public static <E extends Entity> E createEntity(
      Class<E> cls, Map<Locale, String> labelMap, Map<Locale, String> descriptionMap)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    E entity = cls.getConstructor().newInstance();
    LocalizedText labelText = createLocalizedText(labelMap);
    entity.setLabel(labelText);

    LocalizedStructuredContent descriptionContent = new LocalizedStructuredContent();
    for (Map.Entry<Locale, String> entry : descriptionMap.entrySet()) {
      StructuredContent structuredContent = new StructuredContent();
      Paragraph paragraph = new Paragraph(entry.getValue());
      structuredContent.addContentBlock(paragraph);
      descriptionContent.put(entry.getKey(), structuredContent);
    }
    entity.setDescription(descriptionContent);

    return entity;
  }

  public static DigitalObject createDigitalObject(
      Map<Locale, String> labelMap, Map<Locale, String> descriptionMap)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    return createEntity(DigitalObject.class, labelMap, descriptionMap);
  }

  public static EntityRelation createEntityRelation(
      Map<Locale, String> subjectLabels, Map<Locale, String> objectLabels, String predicate)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    Entity subject = createEntity(Entity.class, subjectLabels, Map.of());
    subject.setUuid(UUID.randomUUID());
    Entity object = createEntity(Entity.class, objectLabels, Map.of());
    object.setUuid(UUID.randomUUID());
    return new EntityRelation(subject, predicate, object);
  }

  public static License createLicense(
      String acronym, Map<Locale, String> objectLabels, String url) {
    try {
      License license =
          new License(acronym, createLocalizedText(objectLabels), URI.create(url).toURL());
      return license;
    } catch (MalformedURLException ex) {
      return null;
    }
  }

  private static LocalizedText createLocalizedText(Map<Locale, String> localizedTextMap) {
    LocalizedText localizedText = new LocalizedText();
    for (Map.Entry<Locale, String> entry : localizedTextMap.entrySet()) {
      localizedText.setText(entry.getKey(), entry.getValue());
    }
    return localizedText;
  }

  private TestModelFixture() {}
}
