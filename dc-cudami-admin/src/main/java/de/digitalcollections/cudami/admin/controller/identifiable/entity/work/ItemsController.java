package de.digitalcollections.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiItemsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.item.Item;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/** Controller for item management pages. */
@Controller
public class ItemsController {
  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiItemsClient service;

  public ItemsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forItems();
  }

  @GetMapping("/items")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "items/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "items";
  }

  @GetMapping("/items/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Item item = service.getByUuid(uuid);
    if (item == null) {
      throw new ResourceNotFoundException();
    }

    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, item.getLabel().getLocales());
    List<Locale> existingDigitalObjectLanguages =
        languageSortingHelper.sortLanguages(
            displayLocale, service.getLanguagesOfDigitalObjects(uuid));
    model
        .addAttribute("defaultLanguage", localeService.getDefaultLanguage().getLanguage())
        .addAttribute("item", item)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("existingDigitalObjectLanguages", existingDigitalObjectLanguages);
    return "items/view";
  }
}
