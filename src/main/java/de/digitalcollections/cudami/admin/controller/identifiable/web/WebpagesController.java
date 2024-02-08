package de.digitalcollections.cudami.admin.controller.identifiable.web;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.cudami.client.identifiable.web.CudamiWebpagesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for webpage management pages. */
@Controller
public class WebpagesController
    extends AbstractIdentifiablesController<Webpage, CudamiWebpagesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpagesController.class);
  private final CudamiWebsitesClient websiteService;

  public WebpagesController(CudamiClient client, LanguageService languageService) {
    super(client.forWebpages(), client, languageService);
    this.websiteService = client.forWebsites();
  }

  @GetMapping("/webpages/new")
  public String create(
      Model model,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") UUID parentUuid)
      throws TechnicalException {
    model
        .addAttribute("activeLanguage", languageService.getDefaultLanguage())
        .addAttribute("parentType", parentType)
        .addAttribute("parentUuid", parentUuid);

    Website website = getWebsite(parentUuid, parentType);
    if (website != null) {
      model.addAttribute("parentWebsite", website);
    }

    return "webpages/create";
  }

  @GetMapping("/webpages/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Webpage webpage = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, webpage.getLabel().getLocales());

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

  private Website getWebsite(UUID uuid, String parentType) throws TechnicalException {
    if (parentType == null || "webpage".equals(parentType.toLowerCase())) {
      return ((CudamiWebpagesClient) service).getWebsite(uuid);
    } else if ("website".equals(parentType.toLowerCase())) {
      return websiteService.getByUuid(uuid);
    }
    return null;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "webpages";
  }

  @GetMapping("/webpages/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Webpage webpage = service.getByUuid(uuid);
    if (webpage == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("webpage", webpage);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(webpage);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    List<Locale> existingSubpageLanguages =
        getExistingLanguagesFromIdentifiables(webpage.getChildren());
    String dataLanguageSubpages =
        getDataLanguage(targetDataLanguage, existingSubpageLanguages, languageService);
    model
        .addAttribute("existingSubpageLanguages", existingSubpageLanguages)
        .addAttribute("dataLanguageSubpages", dataLanguageSubpages);

    //    List<FileResource> relatedFileResources =
    //        ((CudamiWebpagesClient) service).findRelatedFileResources(uuid);
    //    model.addAttribute("relatedFileResources", relatedFileResources);

    BreadcrumbNavigation breadcrumbNavigation =
        ((CudamiWebpagesClient) service).getBreadcrumbNavigation(uuid);
    List<BreadcrumbNode> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    // Cut out first breadcrumb node (the one with empty uuid), which identifies the website, since
    // it is handled individually
    breadcrumbs.removeIf(n -> n.getTargetId() == null);
    model.addAttribute("breadcrumbs", breadcrumbs);

    Website website = ((CudamiWebpagesClient) service).getWebsite(uuid);
    model.addAttribute("website", website);

    return "webpages/view";
  }
}
