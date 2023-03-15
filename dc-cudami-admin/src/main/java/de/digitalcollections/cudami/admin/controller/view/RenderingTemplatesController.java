package de.digitalcollections.cudami.admin.controller.view;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.view.CudamiRenderingTemplatesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingTemplate;

/** Controller for rendering template management pages. */
@Controller
public class RenderingTemplatesController
    extends AbstractPagingAndSortingController<RenderingTemplate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderingTemplatesController.class);

  public RenderingTemplatesController(LanguageService languageService, CudamiClient client) {
    super(client.forRenderingTemplates(), languageService);
  }

  @GetMapping("/renderingtemplates/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "renderingtemplates/create";
  }

  @GetMapping("/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws TechnicalException {
    RenderingTemplate template = service.getByUuid(uuid);
    Locale defaultLanguage = languageService.getDefaultLanguage();

    Set<Locale> existingLanguages = new LinkedHashSet<>();
    LocalizedText label = template.getLabel();
    LocalizedText description = template.getDescription();
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

    model
        .addAttribute(
            "activeLanguage", existingLanguages.stream().findFirst().orElse(defaultLanguage))
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("name", template.getName())
        .addAttribute("uuid", template.getUuid());
    return "renderingtemplates/edit";
  }

  @GetMapping("/renderingtemplates")
  public String list(Model model) throws TechnicalException {
    List<Locale> existingLanguages =
        languageService.getExistingLanguagesForLocales(((CudamiRenderingTemplatesClient) service).getLanguages());
    model.addAttribute("existingLanguages", existingLanguages);

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "renderingtemplates/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "renderingtemplates";
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
