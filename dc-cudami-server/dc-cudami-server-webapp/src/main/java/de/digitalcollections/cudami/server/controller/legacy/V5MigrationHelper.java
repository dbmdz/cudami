package de.digitalcollections.cudami.server.controller.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageResponse;

public class V5MigrationHelper {

  public static String migrateToV5(
      PageResponse<Identifiable> pageResponse, ObjectMapper objectMapper)
      throws JsonProcessingException {
    // add "query": "hallo" to request JSON instead of "executedSearchTerm"/"searchTerm": "hallo"
    String result = objectMapper.writeValueAsString(pageResponse);
    result = result.replaceAll("executedSearchTerm", "query");
    result = result.replaceAll("searchTerm", "query");
    return result;
  }

  private V5MigrationHelper() {}
}
