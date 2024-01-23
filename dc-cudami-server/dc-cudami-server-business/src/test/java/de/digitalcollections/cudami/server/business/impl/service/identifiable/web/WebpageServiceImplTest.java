package de.digitalcollections.cudami.server.business.impl.service.identifiable.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The WebpageService")
class WebpageServiceImplTest extends AbstractServiceImplTest {

  private static final Locale FALLBACK_LOCALE = Locale.ENGLISH;

  IdentifierService identifierService;

  WebpageRepository repo;
  WebpageServiceImpl service;

  UrlAliasService urlAliasService;

  @Override
  @BeforeEach
  public void beforeEach() throws Exception {
    super.beforeEach();
    repo = mock(WebpageRepository.class);
    identifierService = mock(IdentifierService.class);
    urlAliasService = mock(UrlAliasService.class);

    LocaleService localeService = mock(LocaleService.class);

    service =
        new WebpageServiceImpl(
            repo, identifierService, urlAliasService, localeService, cudamiConfig);
  }

  @Test
  @DisplayName("fills all active children, when the children tree is requested")
  public void fillActiveChildrentree() throws RepositoryException, ServiceException {
    Webpage parent = createWebpage("00000000-0000-0000-0000-000000000001");

    Webpage child1 = createWebpage("00000000-0000-0000-0000-000000000011");
    parent.addChild(child1);
    Webpage child1Child1 = createWebpage("00000000-0000-0000-0000-000000000111");
    child1.addChild(child1Child1);

    Webpage child2 = createWebpage("00000000-0000-0000-0000-000000000012");
    parent.addChild(child2);
    Webpage child2Child1 = createWebpage("00000000-0000-0000-0000-000000000121");
    child2.addChild(child2Child1);
    Webpage child2Child1Child1 = createWebpage("00000000-0000-0000-0000-000000001211");
    child2Child1.addChild(child2Child1Child1);

    // Child 1 has got one or more subchildren
    PageResponse<Webpage> child1childrenPageResponse = mock(PageResponse.class);
    when(child1childrenPageResponse.getContent()).thenReturn(List.of(child1Child1));
    when(repo.findChildren(eq(child1), any(PageRequest.class)))
        .thenReturn(child1childrenPageResponse);
    when(repo.findChildren(eq(child1Child1), any(PageRequest.class)))
        .thenReturn(mock(PageResponse.class));

    // Child 2 has got one or more subchildren, of which the first child has got children, too
    PageResponse<Webpage> child2childrenPageResponse = mock(PageResponse.class);
    when(child2childrenPageResponse.getContent()).thenReturn(List.of(child2Child1));
    when(repo.findChildren(eq(child2), any(PageRequest.class)))
        .thenReturn(child2childrenPageResponse);
    PageResponse<Webpage> child2Child1childrenPageResponse = mock(PageResponse.class);
    when(child2Child1childrenPageResponse.getContent()).thenReturn(List.of(child2Child1Child1));
    when(repo.findChildren(eq(child2Child1), any(PageRequest.class)))
        .thenReturn(child2Child1childrenPageResponse);
    when(repo.findChildren(eq(child2Child1Child1), any(PageRequest.class)))
        .thenReturn(mock(PageResponse.class));

    // Parent Webpage has got two children.
    PageResponse<Webpage> parentPageResponse = mock(PageResponse.class);
    when(parentPageResponse.getContent()).thenReturn(List.of(child1, child2));
    when(repo.findChildren(eq(parent), any(PageRequest.class))).thenReturn(parentPageResponse);

    List<Webpage> actual = service.getActiveChildrenTree(parent);
    assertThat(actual.get(0).getChildren()).isNotEmpty();
    assertThat(actual.get(1).getChildren().get(0).getChildren()).isNotEmpty();
  }

  @Test
  @DisplayName("fills all children, when the children tree is requested")
  public void fillChildrentree() throws RepositoryException, ServiceException {
    Webpage parent = createWebpage();

    Webpage child1 = createWebpage();
    parent.addChild(child1);
    Webpage child1Child1 = createWebpage();
    child1.addChild(child1Child1);

    Webpage child2 = createWebpage();
    parent.addChild(child2);
    Webpage child2Child1 = createWebpage();
    child2.addChild(child2Child1);
    Webpage child2Child1Child1 = createWebpage();
    child2Child1.addChild(child2Child1Child1);

    // Child 1 has got one or more subchildren
    when(repo.getChildren(eq(child1))).thenReturn(List.of(child1Child1));

    // Child 2 has got one or more subchildren, of which the first child has got children, too
    when(repo.getChildren(eq(child2))).thenReturn(List.of(child2Child1));
    when(repo.getChildren(eq(child2Child1))).thenReturn(List.of(child2Child1Child1));

    // Parent Webpage has got two children.
    when(repo.getChildren(eq(parent))).thenReturn(List.of(child1, child2));

    List<Webpage> actual = service.getChildrenTree(parent);
    assertThat(actual.get(0).getChildren()).isNotEmpty();
    assertThat(actual.get(1).getChildren().get(0).getChildren()).isNotEmpty();
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
  @DisplayName("rejects UrlAliases with empty webpage at save and update")
  public void rejectsEmptyWebpage() {
    Webpage webpage = new Webpage();
    UrlAlias urlAlias1 = new UrlAlias();
    urlAlias1.setTargetLanguage(Locale.GERMAN);
    urlAlias1.setSlug("foo");
    urlAlias1.setPrimary(true);
    Website website = new Website();
    website.setUuid(UUID.randomUUID());
    urlAlias1.setWebsite(website);
    UrlAlias urlAlias2 = new UrlAlias();
    urlAlias2.setTargetLanguage(Locale.GERMAN);
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

  @Test
  @DisplayName("does not allow empty UrlAliases at save")
  public void saveWithEmptyUrlAliases() throws RepositoryException {
    Webpage webpage = new Webpage();
    webpage.setLabel("test");
    assertThrows(
        ServiceException.class,
        () -> {
          service.save(webpage);
        });
    verify(repo, times(1)).save(any(Webpage.class));
  }

  @Test
  @DisplayName("does not allow empty UrlAliases at update")
  public void updateWithEmptyUrlAliases()
      throws ValidationException, ServiceException, RepositoryException {
    UUID webpageUuid = UUID.randomUUID();

    // in DB
    Webpage dbWebpage = new Webpage();
    dbWebpage.setUuid(webpageUuid);
    dbWebpage.setLabel("test");
    when(repo.getByExample(eq(dbWebpage))).thenReturn(dbWebpage);

    Webpage webpage = new Webpage();
    webpage.setLabel("test");
    webpage.setUuid(webpageUuid);
    UrlAlias dummyAlias = new UrlAlias();
    Website dummyWebsite = new Website();
    dummyWebsite.setUuid(UUID.randomUUID());
    dummyAlias.setWebsite(dummyWebsite);
    assertThrows(
        ServiceException.class,
        () -> {
          service.update(webpage);
        });
    verify(repo, times(1)).update(any(Webpage.class));
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
}
