package io.github.dbmdz.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiItemsClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiManifestationsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.item.Item;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import io.github.dbmdz.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
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

/** Controller for item management pages. */
@Controller
public class ItemsController extends AbstractEntitiesController<Item, CudamiItemsClient> {
  private final CudamiManifestationsClient manifestationsService;

  public ItemsController(CudamiClient client, LanguageService languageService) {
    super(client.forItems(), client, languageService);
    this.manifestationsService = client.forManifestations();
  }

  @GetMapping("/items")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
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
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    Locale displayLocale = LocaleContextHolder.getLocale();

    List<Locale> existingDigitalObjectsLanguages =
        languageService.sortLanguages(
            displayLocale, ((CudamiItemsClient) service).getLanguagesOfDigitalObjects(uuid));
    String dataLanguageDigitalObjects =
        getDataLanguage(targetDataLanguage, existingDigitalObjectsLanguages, languageService);
    model
        .addAttribute("existingDigitalObjectsLanguages", existingDigitalObjectsLanguages)
        .addAttribute("dataLanguageDigitalObjects", dataLanguageDigitalObjects);

    return "items/view";
  }
}
