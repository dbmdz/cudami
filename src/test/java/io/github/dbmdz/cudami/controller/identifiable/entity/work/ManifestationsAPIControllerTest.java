package io.github.dbmdz.cudami.controller.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.model.InvertedRelationSpecification;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Manifestations APIController")
class ManifestationsAPIControllerTest {

  private ManifestationsAPIController controller;
  private LanguageService languageService;
  private CudamiClient cudamiClient;

  @BeforeEach
  public void beforeEach() {
    languageService = mock(LanguageService.class);
    cudamiClient = mock(CudamiClient.class);
    controller = new ManifestationsAPIController(cudamiClient, languageService);
  }

  @DisplayName(
      "can return properly filled InvertedRelationSpecifications from a list of manifestations")
  @Test
  public void invertedRelationSpecifications() {
    Manifestation parent1 =
        Manifestation.builder()
            .uuid(UUID.randomUUID())
            .label(new LocalizedText(Locale.GERMAN, "Parent 1"))
            .build();
    Manifestation parent2 =
        Manifestation.builder()
            .uuid(UUID.randomUUID())
            .label(new LocalizedText(Locale.GERMAN, "Parent 2"))
            .build();

    Manifestation childManifestation1 =
        Manifestation.builder()
            .uuid(UUID.randomUUID())
            .label(new LocalizedText(Locale.GERMAN, "Child 1"))
            .parents(
                List.of(
                    new RelationSpecification<Manifestation>("Kind 1", "sortkey1", parent1),
                    new RelationSpecification<Manifestation>("Kid 1", "sortkey1", parent2)))
            .build();
    Manifestation childManifestation2 =
        Manifestation.builder()
            .uuid(UUID.randomUUID())
            .label(new LocalizedText(Locale.GERMAN, "Child 2"))
            .parents(
                List.of(new RelationSpecification<Manifestation>("Kind 2", "sortkey2", parent1)))
            .build();

    List<Manifestation> childManifestations = List.of(childManifestation1, childManifestation2);
    PageResponse<Manifestation> pageResponse =
        PageResponse.builder().withContent(childManifestations).build();

    List<InvertedRelationSpecification> expectedContent =
        List.of(
            new InvertedRelationSpecification("Kind 1", "sortkey1", childManifestation1),
            new InvertedRelationSpecification("Kind 2", "sortkey2", childManifestation2));
    PageResponse<InvertedRelationSpecification> expected =
        PageResponse.builder().withContent(expectedContent).build();
    assertThat(
            controller
                .transformToInvertedRelationSpecification(parent1.getUuid(), pageResponse)
                .getContent())
        .isEqualTo(expected.getContent());
  }
}
