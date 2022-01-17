package de.digitalcollections.cudami.server.business.impl.service.identifiable.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The webservice implementation")
class WebpageServiceImplTest {

  WebpageServiceImpl service;

  WebpageRepository repo;

  IdentifierRepository identifierRepository;
  UrlAliasService urlAliasService;
  CudamiConfig cudamiConfig;

  private static final Locale FALLBACK_LOCALE = Locale.ENGLISH;

  @BeforeEach
  public void beforeEach() {
    repo = mock(WebpageRepository.class);
    identifierRepository = mock(IdentifierRepository.class);
    urlAliasService = mock(UrlAliasService.class);
    cudamiConfig = mock(CudamiConfig.class);
    service = new WebpageServiceImpl(repo, identifierRepository, urlAliasService, cudamiConfig);
  }

  @Test
  @DisplayName("shall not do anything on a node without any label")
  public void ignoreNodeWithNoLabels() {
    Node node = new Node();
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node.getLabel());
    assertThat(node.getLabel()).isNull();
  }

  @Test
  @DisplayName("shall not do anything on a node without any localized label")
  public void ignoreNodeWithNoLocalizedLabels() {
    Node node = new Node();
    LocalizedText emptyLabel = new LocalizedText();

    node.setLabel(emptyLabel);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node.getLabel());
    assertThat(node.getLabel()).isEqualTo(emptyLabel);
  }

  @Test
  @DisplayName("shall use the fallback locale for a localized label, if possible")
  public void useFallbackLocale() {
    Node node = new Node();
    LocalizedText label = new LocalizedText();
    label.setText(FALLBACK_LOCALE, "Test");

    node.setLabel(label);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node.getLabel());
    assertThat(node.getLabel()).isEqualTo(label);
  }

  @Test
  @DisplayName(
      "shall use the first locale (alphabetically) as fallback for a localized label, if possible")
  public void useFirstLocaleAsFallback() {
    LocalizedText expectedFirstLocaleLabel = new LocalizedText(Locale.FRENCH, "faux");

    Node node = new Node();
    LocalizedText label = new LocalizedText();
    label.setText(Locale.ITALIAN, "vero");
    label.setText(Locale.FRENCH, "faux");

    node.setLabel(label);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node.getLabel());
    assertThat(node.getLabel()).isEqualTo(expectedFirstLocaleLabel);
  }

  @Test
  @DisplayName("shall return a label in the desired locale, if possible")
  public void useDesiredLocale() {
    LocalizedText expectedGermanLabel = new LocalizedText(Locale.GERMAN, "richtig");

    Node node = new Node();
    LocalizedText label = new LocalizedText();
    label.setText(FALLBACK_LOCALE, "falsch");
    label.setText(Locale.GERMAN, "richtig");

    node.setLabel(label);
    service.cleanupLabelFromUnwantedLocales(Locale.GERMAN, FALLBACK_LOCALE, node.getLabel());
    assertThat(node.getLabel()).isEqualTo(expectedGermanLabel);
  }

  @Test
  @DisplayName("fills all children, when the children tree is requested")
  public void fillChildrentree() {
    UUID parentUuid = UUID.randomUUID();
    UUID child1Uuid = UUID.randomUUID();
    UUID child2Uuid = UUID.randomUUID();
    UUID child2Child1Uuid = UUID.randomUUID();

    // Child 1 has got one or more subchildren
    Webpage child1 = mock(Webpage.class);
    when(child1.getUuid()).thenReturn(child1Uuid);
    Webpage child1child1 = mock(Webpage.class);
    when(child1.getChildren()).thenReturn(List.of(child1child1));
    when(repo.getChildren(eq(child1Uuid))).thenReturn(List.of(child1child1));

    // Child 2 has got one or more subchildren, of which the first child has got children, too
    Webpage child2 = mock(Webpage.class);
    when(child2.getUuid()).thenReturn(child2Uuid);
    Webpage child2child1 = mock(Webpage.class);
    Webpage child2child1child1 = mock(Webpage.class);
    when(child2child1.getUuid()).thenReturn(child2Child1Uuid);
    when(child2child1.getChildren()).thenReturn(List.of(child2child1child1));
    when(child2.getChildren()).thenReturn(List.of(child2child1));
    when(repo.getChildren(eq(child2Uuid))).thenReturn(List.of(child2child1));
    when(repo.getChildren(eq(child2Child1Uuid))).thenReturn(List.of(child2child1child1));

    // Parent Webpage has got two children.
    when(repo.getChildren(eq(parentUuid))).thenReturn(List.of(child1, child2));

    List<Webpage> actual = service.getChildrenTree(parentUuid);
    assertThat(actual.get(0).getChildren()).isNotEmpty();
    assertThat(actual.get(1).getChildren().get(0).getChildren()).isNotEmpty();
  }

  @Test
  @DisplayName("fills all active children, when the children tree is requested")
  public void fillActiveChildrentree() {
    UUID parentUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID child1Uuid = UUID.fromString("00000000-0000-0000-0000-000000000011");
    UUID child1Child1Uuid = UUID.fromString("00000000-0000-0000-0000-000000000111");
    UUID child2Uuid = UUID.fromString("00000000-0000-0000-0000-000000000012");
    UUID child2Child1Uuid = UUID.fromString("00000000-0000-0000-0000-000000000121");
    UUID child2Child1Child1Uuid = UUID.fromString("00000000-0000-0000-0000-000000001211");

    // Child 1 has got one or more subchildren
    Webpage child1 = mock(Webpage.class);
    when(child1.getUuid()).thenReturn(child1Uuid);
    Webpage child1child1 = mock(Webpage.class);
    when(child1child1.getUuid()).thenReturn(child1Child1Uuid);
    when(child1.getChildren()).thenReturn(List.of(child1child1));
    PageResponse<Webpage> child1childrenPageResponse = mock(PageResponse.class);
    when(child1childrenPageResponse.getContent()).thenReturn(List.of(child1child1));
    when(repo.getChildren(eq(child1Uuid), any(PageRequest.class)))
        .thenReturn(child1childrenPageResponse);
    when(repo.getChildren(eq(child1Child1Uuid), any(PageRequest.class)))
        .thenReturn(mock(PageResponse.class));

    // Child 2 has got one or more subchildren, of which the first child has got children, too
    Webpage child2 = mock(Webpage.class);
    when(child2.getUuid()).thenReturn(child2Uuid);
    Webpage child2child1 = mock(Webpage.class);
    Webpage child2child1child1 = mock(Webpage.class);
    when(child2child1child1.getUuid()).thenReturn(child2Child1Child1Uuid);
    when(child2child1.getUuid()).thenReturn(child2Child1Uuid);
    when(child2child1.getChildren()).thenReturn(List.of(child2child1child1));
    when(child2.getChildren()).thenReturn(List.of(child2child1));
    PageResponse<Webpage> child2childrenPageResponse = mock(PageResponse.class);
    when(child2childrenPageResponse.getContent()).thenReturn(List.of(child2child1));
    when(repo.getChildren(eq(child2Uuid), any(PageRequest.class)))
        .thenReturn(child2childrenPageResponse);
    PageResponse<Webpage> child2Child1childrenPageResponse = mock(PageResponse.class);
    when(child2Child1childrenPageResponse.getContent()).thenReturn(List.of(child2child1child1));
    when(repo.getChildren(eq(child2Child1Uuid), any(PageRequest.class)))
        .thenReturn(child2Child1childrenPageResponse);
    when(repo.getChildren(eq(child2Child1Child1Uuid), any(PageRequest.class)))
        .thenReturn(mock(PageResponse.class));

    // Parent Webpage has got two children.
    PageResponse<Webpage> parentPageResponse = mock(PageResponse.class);
    when(parentPageResponse.getContent()).thenReturn(List.of(child1, child2));
    when(repo.getChildren(eq(parentUuid), any(PageRequest.class))).thenReturn(parentPageResponse);

    List<Webpage> actual = service.getActiveChildrenTree(parentUuid);
    assertThat(actual.get(0).getChildren()).isNotEmpty();
    assertThat(actual.get(1).getChildren().get(0).getChildren()).isNotEmpty();
  }

  @Test
  @DisplayName("does not allow empty UrlAliases at save")
  public void saveWithEmptyUrlAliases()
      throws ValidationException, IdentifiableServiceException, CudamiServiceException {
    Webpage webpage = new Webpage();
    webpage.setLabel("test");
    when(repo.save(eq(webpage))).thenReturn(webpage);
    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.save(webpage);
        });
    verify(repo, times(1)).save(any(Webpage.class));
  }

  @Test
  @DisplayName("does not allow empty UrlAliases at update")
  public void updateWithEmptyUrlAliases()
      throws ValidationException, IdentifiableServiceException, CudamiServiceException {
    UUID webpage_uuid = UUID.randomUUID();

    // in DB
    Webpage dbWebpage = new Webpage();
    dbWebpage.setUuid(webpage_uuid);
    dbWebpage.setLabel("test");
    when(repo.findOne(eq(webpage_uuid))).thenReturn(dbWebpage);

    Webpage webpage = new Webpage();
    webpage.setLabel("test");
    webpage.setUuid(webpage_uuid);
    when(repo.update(eq(webpage))).thenReturn(webpage);
    UrlAlias dummyAlias = new UrlAlias();
    Website dummyWebsite = new Website();
    dummyWebsite.setUuid(UUID.randomUUID());
    dummyAlias.setWebsite(dummyWebsite);
    when(urlAliasService.create(any(UrlAlias.class))).thenReturn(dummyAlias);
    assertThrows(
        IdentifiableServiceException.class,
        () -> {
          service.update(webpage);
        });
    verify(repo, times(1)).update(any(Webpage.class));
  }

  @Test
  @DisplayName("rejects UrlAliases with empty webpage at save and update")
  public void rejectsEmptyWebpage() {
    Webpage webpage = new Webpage();
    UrlAlias urlAlias1 = new UrlAlias();
    urlAlias1.setSlug("foo");
    urlAlias1.setPrimary(true);
    Website website = new Website();
    website.setUuid(UUID.randomUUID());
    urlAlias1.setWebsite(website);
    UrlAlias urlAlias2 = new UrlAlias();
    urlAlias2.setSlug("bar");
    urlAlias2.setPrimary(false);
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases(urlAlias1, urlAlias2);
    webpage.setLocalizedUrlAliases(localizedUrlAliases);

    assertThrows(
        ValidationException.class,
        () -> {
          service.save(webpage);
        });

    assertThrows(
        ValidationException.class,
        () -> {
          service.update(webpage);
        });
  }
}
