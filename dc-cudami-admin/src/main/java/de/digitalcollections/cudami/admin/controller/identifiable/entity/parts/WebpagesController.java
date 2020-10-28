package de.digitalcollections.cudami.admin.controller.identifiable.entity.parts;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.CudamiWebpagesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  public WebpagesController(
      LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forWebpages();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "webpages";
  }

  @GetMapping("/webpages/new")
  public String create(
      Model model,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") String parentUuid)
      throws HttpException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
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
      throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Webpage webpage = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, webpage.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", webpage.getUuid());

    return "webpages/edit";
  }

  @GetMapping("/api/webpages/{uuid}")
  @ResponseBody
  public Webpage get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/api/webpages/{uuid}/webpages")
  @ResponseBody
  public PageResponse<WebpageImpl> getSubpages(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    return service.getChildren(uuid, new PageRequestImpl(pageNumber, pageSize));
  }

  @PostMapping("/api/webpages/new")
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
    } catch (HttpException e) {
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
      Webpage webpageDb = (Webpage) service.update(uuid, webpage);
      return ResponseEntity.ok(webpageDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save webpage with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/webpages/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Webpage webpage = service.findOne(uuid);
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

    List<FileResource> relatedFileResources = service.getRelatedFileResources(uuid);
    model.addAttribute("relatedFileResources", relatedFileResources);

    BreadcrumbNavigation breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);

    List<Node> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    // Cut out first breadcrumb node (the one with empty uuid), which identifies the website, since
    // it is
    // handled individually
    breadcrumbs.removeIf(n -> n.getUuid() == null);
    model.addAttribute("breadcrumbs", breadcrumbs);

    Website website = service.getWebsite(uuid);
    model.addAttribute("website", website);

    return "webpages/view";
  }
}
