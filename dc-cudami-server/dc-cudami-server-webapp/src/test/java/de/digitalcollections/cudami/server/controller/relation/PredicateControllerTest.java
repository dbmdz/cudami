package de.digitalcollections.cudami.server.controller.relation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(PredicateController.class)
@DisplayName("The PredicateController")
class PredicateControllerTest extends BaseControllerTest {

  @MockBean private PredicateService predicateService;

  @DisplayName("can save a predicate without UUID")
  @Test
  public void save() throws Exception {
    UUID uuid = UUID.fromString("bb5885f6-fd24-48e9-99f6-1f1a49d239bf");
    Predicate predicate =
        Predicate.builder()
            .value("foo")
            .label(new LocalizedText(Locale.ROOT, "bar"))
            .uuid(uuid)
            .build();

    // We save a predicate without UUID
    String jsonBody =
        "{\"objectType\":\"PREDICATE\",\"value\":\"foo\",\"label\": {" + "\"\": \"bar\"" + "}}";

    when(predicateService.save(any(Predicate.class))).thenReturn(predicate);

    testPostJson("/v6/predicates", jsonBody, "/v6/relation/predicates/predicates_response.json");
    testPostJson("/v5/predicates", jsonBody, "/v6/relation/predicates/predicates_response.json");
    testPostJson("/v3/predicates", jsonBody, "/v6/relation/predicates/predicates_response.json");
    testPostJson(
        "/latest/predicates", jsonBody, "/v6/relation/predicates/predicates_response.json");
  }

  @DisplayName(
      "can save a predicate without UUID but with value in path (old style, where save and update were equal)")
  @Test
  public void saveWithValueInPath() throws Exception {
    UUID uuid = UUID.fromString("bb5885f6-fd24-48e9-99f6-1f1a49d239bf");
    Predicate predicate =
        Predicate.builder()
            .value("foo")
            .label(new LocalizedText(Locale.ROOT, "bar"))
            .uuid(uuid)
            .build();

    // We save a predicate without UUID
    String jsonBody =
        "{\"objectType\":\"PREDICATE\",\"value\":\"foo\",\"label\": {" + "\"\": \"bar\"" + "}}";

    when(predicateService.save(any(Predicate.class))).thenReturn(predicate);

    // Yes, it really works with put!
    testPutJson("/v6/predicates/foo", jsonBody, "/v6/relation/predicates/predicates_response.json");
    testPutJson("/v5/predicates/foo", jsonBody, "/v6/relation/predicates/predicates_response.json");
    testPutJson("/v3/predicates/foo", jsonBody, "/v6/relation/predicates/predicates_response.json");
    testPutJson(
        "/latest/predicates/foo", jsonBody, "/v6/relation/predicates/predicates_response.json");
  }

  @DisplayName("can update a predicate by its value, without uuid (used by old clients)")
  @Test
  public void updateByValueWithoutUuid() throws Exception {
    UUID uuid = UUID.fromString("bb5885f6-fd24-48e9-99f6-1f1a49d239bf");
    Predicate predicate =
        Predicate.builder()
            .value("foo")
            .label(new LocalizedText(Locale.ROOT, "bar"))
            .uuid(uuid)
            .build();

    // We update a predicate without UUID
    String jsonBody =
        "{\"objectType\":\"PREDICATE\",\"value\":\"foo\",\"label\": {" + "\"\": \"bar\"" + "}}";
    when(predicateService.save(any(Predicate.class))).thenReturn(predicate);

    testPutJson("/v6/predicates/foo", jsonBody, "/v6/relation/predicates/predicates_response.json");
  }

  @DisplayName("can update a predicate by its uuid")
  @Test
  public void updateByUuid() throws Exception {
    UUID uuid = UUID.fromString("bb5885f6-fd24-48e9-99f6-1f1a49d239bf");
    Predicate predicate =
        Predicate.builder()
            .value("foo")
            .label(new LocalizedText(Locale.ROOT, "bar"))
            .uuid(uuid)
            .build();

    // We update a predicate without UUID
    String jsonBody =
        "{\"uuid\":\""
            + uuid
            + "\",\"objectType\":\"PREDICATE\",\"value\":\"foo\",\"label\": {"
            + "\"\": \"bar\""
            + "}}";
    when(predicateService.save(any(Predicate.class))).thenReturn(predicate);

    testPutJson(
        "/v6/predicates/" + uuid, jsonBody, "/v6/relation/predicates/predicates_response.json");
  }
}
