package de.digitalcollections.cudami.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.PageResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/** Holds serialization helpers for legacy endpoints */
public abstract class AbstractLegacyController {

  private static final Map<String, JsonFix> JSON_FIXES = new HashMap<>();

  static {
    JSON_FIXES.put(
        "collection",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.entity.CollectionImpl", null, null));
    JSON_FIXES.put(
        "digitalObject",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl", null, null));
    JSON_FIXES.put(
        "entityRelation",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.entity.relation.EntityRelationImpl",
            null,
            null));
    JSON_FIXES.put(
        "identifierType",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.IdentifierTypeImpl", null, null));
    JSON_FIXES.put(
        "image",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl",
            null,
            null));
    JSON_FIXES.put(
        "project",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.entity.ProjectImpl", null, null));
    JSON_FIXES.put(
        "renderingTemplate",
        new JsonFix("de.digitalcollections.model.impl.view.RenderingTemplate", null, null));
    JSON_FIXES.put(
        "webpage",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl",
            "ENTITY_PART",
            "WEBPAGE"));
    JSON_FIXES.put(
        "website",
        new JsonFix(
            "de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl", null, null));
  }

  protected final DigitalCollectionsObjectMapper objectMapper =
      new DigitalCollectionsObjectMapper();

  /**
   * Magically fix embedded objects by fixing all children, which are no plain attributes, but
   * objects
   *
   * @param parentObject The parent object to start with
   * @return magically fixed object
   */
  protected JSONObject fixEmbeddedObject(JSONObject parentObject) {
    // Retrieve all attributes, which are objects and fix them recursivly
    for (Iterator<String> it = parentObject.keys(); it.hasNext(); ) {
      String attribute = it.next();
      if (parentObject.get(attribute) instanceof JSONObject) {
        // Attribute is a JSONObject -> fix it recursively
        parentObject.put(attribute, fixEmbeddedObject(parentObject.getJSONObject(attribute)));
      } else if (parentObject.get(attribute) instanceof JSONArray) {
        // Attribute is a JSONArray -> fix all items recursively
        JSONArray attributeArray = parentObject.getJSONArray(attribute);
        JSONArray fixedAttributeArray = new JSONArray();
        for (int i = 0; i < attributeArray.length(); i++) {
          if (attributeArray.get(i) instanceof JSONObject) {
            JSONObject contentObject = attributeArray.getJSONObject(i);
            fixedAttributeArray.put(i, fixEmbeddedObject(contentObject));
          } else {
            // Don't modify it
            fixedAttributeArray.put(i, attributeArray.get(i));
          }
        }
        parentObject.put(attribute, fixedAttributeArray);
      }
    }

    // For the given object, calculate its data type and fix the JSON for it
    for (String objectType : JSON_FIXES.keySet()) {
      if (parentObject.has(objectType)) {
        JSONObject object = parentObject.getJSONObject(objectType);
        JsonFix jsonFix = JSON_FIXES.get(objectType);
        object.put("className", jsonFix.getClassAndPackageName());
        if (StringUtils.hasText(jsonFix.getType())) {
          object.put("type", jsonFix.getType());
        }
        if (StringUtils.hasText(jsonFix.getEntityPartType())) {
          object.put("entityPartType", jsonFix.getEntityPartType());
        }
        object = fixEmbeddedObject(object); // recursively step into object to be fixed
        return object;
      }
    }
    return parentObject;
  }

  protected String fixPageResponse(PageResponse pageResponse) throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(pageResponse));
    jsonObject = fixEmbeddedObject(jsonObject);
    return jsonObject.toString();
  }

  protected String fixSimpleObject(Object object, String type, String entityPartType)
      throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(object));
    jsonObject = fixEmbeddedObject(jsonObject);
    if (StringUtils.hasText(type)) {
      jsonObject.put("type", type);
    }
    if (StringUtils.hasText(entityPartType)) {
      jsonObject.put("entityPartType", entityPartType);
    }
    return jsonObject.toString();
  }

  protected String fixSimpleObjectList(List<CorporateBody> objectList)
      throws JsonProcessingException {
    JSONArray json = new JSONArray();
    int i = 0;
    for (Entity listItem : objectList) {
      json.put(i++, fixSimpleObject(listItem, null, null));
    }

    return json.toString();
  }

  protected static class JsonFix {

    private final String classAndPackageName;
    private final String type;
    private final String entityPartType;

    public JsonFix(String classAndPackageName, String type, String entityPartType) {
      this.classAndPackageName = classAndPackageName;
      this.type = type;
      this.entityPartType = entityPartType;
    }

    public String getClassAndPackageName() {
      return classAndPackageName;
    }

    public String getType() {
      return type;
    }

    public String getEntityPartType() {
      return entityPartType;
    }
  }
}
