package io.github.dbmdz.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiCollectionsClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
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

/** Controller for collection management pages. */
@Controller
public class CollectionsController
    extends AbstractEntitiesController<Collection, CudamiCollectionsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsController.class);

  public CollectionsController(CudamiClient client, LanguageService languageService) {
    super(client.forCollections(), client, languageService);
  }

  @GetMapping("/collections/new")
  public String create(
      Model model,
      @RequestParam(name = "parentType", required = false) String parentType,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid)
      throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "collections/create";
  }

  @GetMapping("/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Collection collection = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, collection.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", collection.getUuid());

    return "collections/edit";
  }

  @GetMapping("/collections")
  public String list(Model model) throws TechnicalException {
    List<Locale> existingLanguages =
        languageService.getExistingLanguagesForLocales(
            ((CudamiCollectionsClient) service).getLanguagesOfTopCollections());
    model.addAttribute("existingLanguages", existingLanguages);

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "collections/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "collections";
  }

  @GetMapping("/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Collection collection = service.getByUuid(uuid);
    if (collection == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("collection", collection);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(collection);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    List<Locale> existingSubcollectionsLanguages =
        getExistingLanguagesFromIdentifiables(collection.getChildren());
    String dataLanguageSubcollections =
        getDataLanguage(targetDataLanguage, existingSubcollectionsLanguages, languageService);
    model
        .addAttribute("existingSubcollectionsLanguages", existingSubcollectionsLanguages)
        .addAttribute("dataLanguageSubcollections", dataLanguageSubcollections);

    List<Collection> parents = ((CudamiCollectionsClient) service).getParents(uuid);
    model.addAttribute("parents", parents);

    BreadcrumbNavigation breadcrumbNavigation =
        ((CudamiCollectionsClient) service).getBreadcrumbNavigation(uuid);
    List<BreadcrumbNode> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    model.addAttribute("breadcrumbs", breadcrumbs);

    return "collections/view";
  }

  @GetMapping("/collections/{refId:[0-9]+}")
  public String viewByRefId(
      @PathVariable long refId,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Collection collection = ((CudamiEntitiesClient<Collection>) service).getByRefId(refId);
    if (collection == null) {
      throw new ResourceNotFoundException();
    }
    return view(collection.getUuid(), targetDataLanguage, model);
  }
}
