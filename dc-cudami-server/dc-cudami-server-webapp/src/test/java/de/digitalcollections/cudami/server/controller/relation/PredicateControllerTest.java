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

  @DisplayName("can create a predicate")
  @Test
  public void create() throws Exception {
    UUID uuid = UUID.fromString("bb5885f6-fd24-48e9-99f6-1f1a49d239bf");
    Predicate predicate =
        Predicate.builder()
            .value("foo")
            .label(new LocalizedText(Locale.ROOT, "bar"))
            .uuid(uuid)
            .build();

    String jsonBody =
        "{\"objectType\":\"PREDICATE\",\"value\":\"foo\",\"label\": {" + "\"\": \"bar\"" + "}}";

    when(predicateService.save(any(Predicate.class))).thenReturn(predicate);

    testPostJson("/v6/predicates", jsonBody, "/v6/relation/predicates/predicates_response.json");
  }
}
