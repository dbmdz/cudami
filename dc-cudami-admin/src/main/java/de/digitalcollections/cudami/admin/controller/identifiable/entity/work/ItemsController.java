package de.digitalcollections.cudami.admin.controller.identifiable.entity.work;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiItemsClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiManifestationsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.item.Item;

/** Controller for item management pages. */
@Controller
public class ItemsController extends AbstractEntitiesController<Item, CudamiItemsClient> {
  private final CudamiManifestationsClient manifestationsService;

  public ItemsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forItems(), languageSortingHelper, client.forLocales());
    this.manifestationsService = client.forManifestations();
  }

  @GetMapping("/items")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, localeService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "items/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "items";
  }

  @GetMapping("/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Item item = service.getByUuid(uuid);
    if (item == null) {
      throw new ResourceNotFoundException();
    }

    if (item.getManifestation() != null) {
      item.setManifestation(manifestationsService.getByUuid(item.getManifestation().getUuid()));
    }

    model.addAttribute("item", item);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(item);
    String dataLanguage = getDataLanguage(targetDataLanguage, localeService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingDigitalObjectsLanguages =
        languageSortingHelper.sortLanguages(
            displayLocale, service.getLanguagesOfDigitalObjects(uuid));
    model
        .addAttribute("existingDigitalObjectsLanguages", existingDigitalObjectsLanguages)
        .addAttribute("dataLanguageDigitalObjects", getDataLanguage(null, localeService));

    return "items/view";
  }
}
