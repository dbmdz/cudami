package de.digitalcollections.cudami.server.controller.legacy;

import static de.digitalcollections.model.list.sorting.Direction.ASC;
import static de.digitalcollections.model.list.sorting.Direction.DESC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.controller.legacy.model.LegacyFiltering;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class V5MigrationHelper {

  public static List<Order> migrate(List<Order> sortBy) {
    List<Order> result = null;
    if (sortBy != null) {
      result =
          sortBy.stream()
              .map(
                  o -> {
                    o.setIgnoreCase(true);
                    return o;
                  })
              .collect(Collectors.toList());
    }
    return result;
  }

  public static String migrate(PageResponse<?> pageResponse, ObjectMapper objectMapper)
      throws JsonProcessingException {
    if (pageResponse == null) {
      return null;
    }

    // For each order of the sorting, we have to re-append the values for ascending and descending
    JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(pageResponse));
    return migrateToV5(jsonObject, objectMapper);
  }

  public static String migrateToV5(String unmigratedJson) {
    String migratedJson = unmigratedJson.replaceAll("executedSearchTerm", "query");
    migratedJson = migratedJson.replaceAll("searchTerm", "query");
    return migratedJson;
  }

  public static String migrateToV5(JSONObject jsonObject, ObjectMapper objectMapper)
      throws JsonMappingException, JsonProcessingException, JSONException {
    if (jsonObject == null) {
      return null;
    }

    if (jsonObject.has("pageRequest")) {
      JSONObject pageRequest = (JSONObject) jsonObject.get("pageRequest");
      if (pageRequest.has("sorting")) {
        JSONObject sorting = (JSONObject) pageRequest.get("sorting");
        if (sorting.has("orders")) {
          JSONArray orders = (JSONArray) sorting.get("orders");
          JSONObject migratedSorting = new JSONObject();
          for (Iterator it = orders.iterator(); it.hasNext(); ) {
            JSONObject order = (JSONObject) it.next();
            Direction direction = Direction.fromString((String) order.get("direction"));
            order.put("ascending", direction == ASC);
            order.put("descending", direction == DESC);
            migratedSorting.append("orders", order);
          }
          pageRequest.put("sorting", migratedSorting);
        }
      }
      if (pageRequest.has("filtering")) {
        LegacyFiltering legacyFiltering =
            new LegacyFiltering(
                objectMapper.readValue(
                    pageRequest.getJSONObject("filtering").toString(), Filtering.class));
        pageRequest.put(
            "filtering", new JSONObject(objectMapper.writeValueAsString(legacyFiltering)));
        if (legacyFiltering.getFilterCriterionFor("label") != null) {
          pageRequest.put("query", legacyFiltering.getFilterCriterionFor("label").getValue());
          jsonObject.put("query", legacyFiltering.getFilterCriterionFor("label").getValue());
        }
      }
      jsonObject.put("pageRequest", pageRequest);
    }

    if (jsonObject.has("content")) {
      JSONArray content = jsonObject.getJSONArray("content");
      content.forEach(
          obj -> {
            if (obj instanceof JSONObject item) {
              if (item.has("entityType")) {
                switch (item.getString("entityType")) {
                  case "DIGITAL_OBJECT":
                    if (item.isNull("fileResources")) item.put("fileResources", new JSONArray());
                    break;
                  case "ARTICLE":
                    if (item.isNull("creators")) item.put("creators", new JSONArray());
                    break;
                  default:
                }
              }
            }
          });
    }

    return migrateToV5(jsonObject.toString());
  }

  private V5MigrationHelper() {}
}
