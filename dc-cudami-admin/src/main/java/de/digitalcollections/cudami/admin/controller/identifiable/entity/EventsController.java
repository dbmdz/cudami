package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEventsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Event;
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

/** Controller for events pages. */
@Controller
public class EventsController extends AbstractEntitiesController<Event, CudamiEventsClient> {

  public EventsController(CudamiClient client, LanguageService languageService) {
    super(client.forEvents(), client, languageService);
  }

  @GetMapping("/events/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "events/create";
  }

  @GetMapping("/events/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Event event = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, event.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", event.getUuid());

    return "events/edit";
  }

  @GetMapping("/events")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "events/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "events";
  }

  @GetMapping("/events/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Event event = service.getByUuid(uuid);
    if (event == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("event", event);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(event);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "events/view";
  }
}
