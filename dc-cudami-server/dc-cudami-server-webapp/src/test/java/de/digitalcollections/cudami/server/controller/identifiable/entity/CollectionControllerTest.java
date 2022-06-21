package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.filtering.Filtering;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
            CorporateBody.builder()
                .created("2020-10-20T14:38:07.757894")
                .identifier("gnd", "1234567-8", "30b59f1e-aa2f-4ae5-b9a4-fa336e21ad8e")
                .label(Locale.GERMAN, "Institution 1")
                .label(Locale.ENGLISH, "institution 1")
                .lastModified("2021-02-25T09:05:34.039316")
                .previewImage(
                    "instituion_logo.svg",
                    "fbd286db-df7c-4170-bc21-2ad943252a1a",
                    "https://commons.wikimedia.org/wiki/Special:FilePath/institution_logo.svg?width=270",
                    MimeType.MIME_IMAGE)
                .altText(Locale.GERMAN, "Logo der Institution 1. Zur Startseite")
                .altText(Locale.ENGLISH, "Logo of the institution 1. Navigate to main page")
                .dontOpenPreviewImageInNewWindow()
                .uuid("2d72fc41-e7a7-4666-a76f-38e3d565eb48")
                .refId(1191463)
                .homepageUrl("https://www.whateveryouwanttotest.de/")
                .build());

    when(collectionService.findRelatedCorporateBodies(any(UUID.class), any(Filtering.class)))
        .thenReturn(expected);

    testJson(
        path, "/v3/collections/09baa24e-0918-4b96-8ab1-f496b02af73a_related_corporatebodies.json");
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/collections/identifier/foo:bar",
        "/v5/collections/identifier/foo:bar",
        "/v2/collections/identifier/foo:bar",
        "/latest/collections/identifier/foo:bar",
        "/v6/collections/identifier/foo:bar.json",
        "/v5/collections/identifier/foo:bar.json",
        "/v2/collections/identifier/foo:bar.json",
        "/latest/collections/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    Collection expected = Collection.builder().build();

    when(collectionService.getByIdentifier(eq("foo"), eq("bar"))).thenReturn(expected);

    testHttpGet(path);

    verify(collectionService, times(1)).getByIdentifier(eq("foo"), eq("bar"));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/collections/identifier/", "/v5/collections/identifier/"})
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    Collection expected = Collection.builder().build();

    when(collectionService.getByIdentifier(eq("foo"), eq("bar/bla"))).thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(collectionService, times(1)).getByIdentifier(eq("foo"), eq("bar/bla"));
  }
}
