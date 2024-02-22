package io.github.dbmdz.cudami.controller.view;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.view.CudamiRenderingTemplatesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingTemplate;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for rendering template management pages. */
@Controller
@SessionAttributes(value = {"renderingTemplate"})
public class RenderingTemplatesController
    extends AbstractUniqueObjectController<RenderingTemplate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderingTemplatesController.class);

  private final MessageSource messageSource;

  public RenderingTemplatesController(
      CudamiClient client, LanguageService languageService, MessageSource messageSource) {
    super(client.forRenderingTemplates(), languageService);
    this.messageSource = messageSource;
  }

  @GetMapping("/renderingtemplates/new")
  public String create(Model model) throws TechnicalException {
    RenderingTemplate renderingTemplate = service.create();
    Locale defaultLanguage = languageService.getDefaultLanguage();
    renderingTemplate.setDescription(new LocalizedText(defaultLanguage, ""));
    renderingTemplate.setLabel(new LocalizedText(defaultLanguage, ""));
    model.addAttribute("renderingTemplate", renderingTemplate);

    List<Locale> existingLanguages = List.of(defaultLanguage);
    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("allLanguages", sortedLanguages);
    model.addAttribute("activeLanguage", defaultLanguage);

    model.addAttribute("mode", "create");
    return "renderingtemplates/create-or-edit";
  }

  @GetMapping("/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    RenderingTemplate template = service.getByUuid(uuid);
    if (template == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("renderingTemplate", template);

    Set<Locale> existingLanguages = getExistingLanguages(template);
    model.addAttribute("existingLanguages", existingLanguages);

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.toArray()[0]);
    }

    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("allLanguages", sortedLanguages);

    model.addAttribute("mode", "edit");
    return "renderingtemplates/create-or-edit";
  }

  private Set<Locale> getExistingLanguages(RenderingTemplate template) throws TechnicalException {
    Locale defaultLanguage = languageService.getDefaultLanguage();
    LocalizedText label = template.getLabel();
    LocalizedText description = template.getDescription();
    Set<Locale> existingLanguages = new LinkedHashSet<>();
    if (CollectionUtils.isEmpty(label) && CollectionUtils.isEmpty(description)) {
      existingLanguages.add(defaultLanguage);
    } else {
      if (!CollectionUtils.isEmpty(label)) {
        existingLanguages.addAll(label.getLocales());
      }
      if (!CollectionUtils.isEmpty(description)) {
        existingLanguages.addAll(description.getLocales());
      }
    }
    Locale displayLocale = LocaleContextHolder.getLocale();
    existingLanguages =
        new LinkedHashSet<>(languageService.sortLanguages(displayLocale, existingLanguages));
    return existingLanguages;
  }

  @GetMapping("/renderingtemplates")
  public String list(Model model) throws TechnicalException {
    List<Locale> existingLanguages =
        languageService.getExistingLanguagesForLocales(
            ((CudamiRenderingTemplatesClient) service).getLanguages());
    model.addAttribute("existingLanguages", existingLanguages);

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "renderingtemplates/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "renderingtemplates";
  }

  @PostMapping("/renderingtemplates/new")
  public String save(
      @ModelAttribute @Valid RenderingTemplate renderingTemplate,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("mode", "create");

    verifyBinding(results);
    if (results.hasErrors()) {
      return "renderingtemplates/create-or-edit";
    }
    try {
      service.save(renderingTemplate);
      LOGGER.info("Successfully saved rendering template");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save rendering template: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/renderingtemplates";
    }
    if (results.hasErrors()) {
      return "renderingtemplates/create-or-edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/renderingtemplates";
  }

  @PostMapping(value = "/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String update(
      @PathVariable UUID uuid,
      @ModelAttribute("formData") RenderingTemplate renderingTemplateFormData,
      @ModelAttribute RenderingTemplate renderingTemplate,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);

    // just update the fields, that were editable
    // needed session attribute independent "formData" because session
    // attributes just get data set, but do not remove hashmap entry (language, if
    // tab is removed)
    renderingTemplate.setLabel(renderingTemplateFormData.getLabel());
    renderingTemplate.setDescription(renderingTemplateFormData.getDescription());

    if (results.hasErrors()) {
      Set<Locale> existingLanguages = getExistingLanguages(renderingTemplate);
      model.addAttribute("existingLanguages", existingLanguages);
      model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      return "renderingtemplates/create-or-edit";
    }

    try {
      service.update(uuid, renderingTemplate);
    } catch (TechnicalException e) {
      String message = "Cannot update renderingTemplate with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/renderingtemplates/" + uuid + "/edit";
    }

    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/renderingtemplates/" + uuid;
  }

  @GetMapping("/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    RenderingTemplate renderingTemplate = service.getByUuid(uuid);
    if (renderingTemplate == null) {
      throw new ResourceNotFoundException();
    }

    List<Locale> existingLanguages = Collections.emptyList();
    LocalizedText label = renderingTemplate.getLabel();
    if (!CollectionUtils.isEmpty(label)) {
      Locale displayLocale = LocaleContextHolder.getLocale();
      existingLanguages = languageService.sortLanguages(displayLocale, label.getLocales());
    }

    String dataLanguage = targetDataLanguage;
    if (dataLanguage == null && languageService != null) {
      dataLanguage = languageService.getDefaultLanguage().getLanguage();
    }

    model
        .addAttribute("renderingTemplate", renderingTemplate)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "renderingtemplates/view";
  }
}
