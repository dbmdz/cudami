package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.CorporateBodyBuilder;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(CollectionController.class)
@DisplayName("The CollectionController")
class CollectionControllerTest extends BaseControllerTest {

  @MockBean private CollectionService collectionService;
  @MockBean private LocaleService localeService;

  @DisplayName("shall return related corporate bodies for a given predicate")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/collections/09baa24e-0918-4b96-8ab1-f496b02af73a/related/corporatebodies?predicate=eq:is_sponsored_by"
      })
  public void relatedByPredicate(String path) throws Exception {
    List<CorporateBody> expected =
        List.of(
            new CorporateBodyBuilder()
                .createdAt("2020-10-20T14:38:07.757894")
                .withIdentifier("gnd", "1234567-8", "30b59f1e-aa2f-4ae5-b9a4-fa336e21ad8e")
                .withLabel(Locale.GERMAN, "Institution 1")
                .withLabel(Locale.ENGLISH, "institution 1")
                .lastModifiedAt("2021-02-25T09:05:34.039316")
                .withPreviewImage(
                    "instituion_logo.svg",
                    "fbd286db-df7c-4170-bc21-2ad943252a1a",
                    "https://commons.wikimedia.org/wiki/Special:FilePath/institution_logo.svg?width=270",
                    MimeType.MIME_IMAGE)
                .withAltText(Locale.GERMAN, "Logo der Institution 1. Zur Startseite")
                .withAltText(Locale.ENGLISH, "Logo of the institution 1. Navigate to main page")
                .withoutOpenPreviewInNewWindow()
                .withUuid("2d72fc41-e7a7-4666-a76f-38e3d565eb48")
                .withRefId(1191463)
                .withHomepageUrl("https://www.whateveryouwanttotest.de/")
                .build());

    when(collectionService.getRelatedCorporateBodies(any(UUID.class), any(Filtering.class)))
        .thenReturn(expected);

    testJson(
        path, "/v3/collections/09baa24e-0918-4b96-8ab1-f496b02af73a_related_corporatebodies.json");
  }
}
