package de.digitalcollections.cudami.server.controller.legacy;

import static de.digitalcollections.model.list.sorting.Direction.ASC;
import static de.digitalcollections.model.list.sorting.Direction.DESC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import java.util.Iterator;

public class V5MigrationHelper {

  private V5MigrationHelper() {}

  public static String migrateToV5(PageResponse<?> pageResponse, ObjectMapper objectMapper)
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

  public static String migrateToV5(JSONObject jsonObject, ObjectMapper objectMapper) {
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
        jsonObject.put("pageRequest", pageRequest);
      }
    }

    return migrateToV5(jsonObject.toString());
  }
}
