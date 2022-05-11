package de.digitalcollections.cudami.server.controller.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.list.paging.PageResponse;

public class V5MigrationHelper {

  public static String migrateToV5(PageResponse<?> pageResponse, ObjectMapper objectMapper)
      throws JsonProcessingException {
    // add "query": "hallo" to request JSON instead of "executedSearchTerm"/"searchTerm": "hallo"
    String jsonPageResponse = objectMapper.writeValueAsString(pageResponse);
    return migrateToV5(jsonPageResponse);
  }

  private V5MigrationHelper() {}

  public static String migrateToV5(String unmigratedJson) {
    String migratedJson = unmigratedJson.replaceAll("executedSearchTerm", "query");
    migratedJson = migratedJson.replaceAll("searchTerm", "query");
    return migratedJson;
  }
}
