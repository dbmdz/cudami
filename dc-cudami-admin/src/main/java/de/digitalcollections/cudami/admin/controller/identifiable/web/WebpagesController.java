package de.digitalcollections.cudami.admin.controller.identifiable.web;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.cudami.client.identifiable.web.CudamiWebpagesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for webpage management pages. */
@Controller
public class WebpagesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpagesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiWebpagesClient service;
  private final CudamiWebsitesClient websiteService;

  public WebpagesController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forWebpages();
    this.websiteService = client.forWebsites();
  }

  @GetMapping("/webpages/new")
  public String create(
      Model model,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") UUID parentUuid)
      throws TechnicalException {
    model
        .addAttribute("activeLanguage", localeService.getDefaultLanguage())
        .addAttribute("parentType", parentType)
        .addAttribute("parentUuid", parentUuid);

    Website website = getWebsite(parentUuid, parentType);
    if (website != null) {
      model.addAttribute("parentWebsite", website);
    }

    return "webpages/create";
  }

  @GetMapping("/api/webpages/new")
  @ResponseBody
  public Webpage create() {
    return service.create();
  }

  @GetMapping("/webpages/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Webpage webpage = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, webpage.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("uuid", webpage.getUuid());

    Website website = getWebsite(uuid, null);
    if (website != null) {
      model.addAttribute("parentWebsite", website);
    }

    return "webpages/edit";
  }

  @GetMapping("/api/webpages/{uuid}/webpages")
  @ResponseBody
  public PageResponse<Webpage> findSubpages(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.findSubpages(uuid, searchPageRequest);
  }

  @GetMapping("/api/webpages/{uuid}")
  @ResponseBody
  public Webpage getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  private Website getWebsite(UUID uuid, String parentType) throws TechnicalException {
    if (parentType == null || "webpage".equals(parentType.toLowerCase())) {
      return service.getWebsite(uuid);
    } else if ("website".equals(parentType.toLowerCase())) {
      return websiteService.getByUuid(uuid);
    }
    return null;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "webpages";
  }

  @PostMapping("/api/webpages")
  public ResponseEntity save(
      @RequestBody Webpage webpage,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") UUID parentUuid) {
    try {
      Webpage webpageDb = null;
      if (parentType.equals("website")) {
        webpageDb = service.saveWithParentWebsite(webpage, parentUuid);
      } else {
        webpageDb = service.saveWithParentWebpage(webpage, parentUuid);
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(webpageDb);
    } catch (TechnicalException e) {
      if (parentType.equals("website")) {
        LOGGER.error("Cannot save top-level webpage: ", e);
      } else if (parentType.equals("webpage")) {
        LOGGER.error("Cannot save webpage: ", e);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/webpages/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Webpage webpage) {
    try {
      Webpage webpageDb = service.update(uuid, webpage);
      return ResponseEntity.ok(webpageDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save webpage with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/webpages/{uuid}/webpages")
  public ResponseEntity updateSubpagesOrder(
      @PathVariable UUID uuid, @RequestBody List<Webpage> subpages) throws TechnicalException {
    boolean successful = service.updateChildrenOrder(uuid, subpages);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/webpages/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Webpage webpage = service.getByUuid(uuid);
    if (webpage == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, webpage.getLabel().getLocales());
    List<Locale> existingSubpageLanguages =
        webpage.getChildren().stream()
            .flatMap(child -> child.getLabel().getLocales().stream())
            .collect(Collectors.toList());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute(
        "existingSubpageLanguages",
        languageSortingHelper.sortLanguages(displayLocale, existingSubpageLanguages));
    model.addAttribute("webpage", webpage);

    List<FileResource> relatedFileResources = service.findRelatedFileResources(uuid);
    model.addAttribute("relatedFileResources", relatedFileResources);

    BreadcrumbNavigation breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);

    List<BreadcrumbNode> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    // Cut out first breadcrumb node (the one with empty uuid), which identifies the website, since
    // it is
    // handled individually
    breadcrumbs.removeIf(n -> n.getTargetId() == null);
    model.addAttribute("breadcrumbs", breadcrumbs);

    Website website = service.getWebsite(uuid);
    model.addAttribute("website", website);

    return "webpages/view";
  }
}
