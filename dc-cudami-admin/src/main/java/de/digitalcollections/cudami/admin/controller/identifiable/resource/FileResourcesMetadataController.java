package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesMetadataClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.resource.FileResource;
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

/** Controller for resource management pages. */
@Controller
public class FileResourcesMetadataController extends AbstractController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourcesMetadataController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiFileResourcesMetadataClient service;

  public FileResourcesMetadataController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forFileResourcesMetadata();
  }

  @GetMapping(value = "/fileresources/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "fileresources/create";
  }

  @GetMapping("/fileresources/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FileResource fileResource = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, fileResource.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("filename", fileResource.getFilename());
    model.addAttribute("uuid", fileResource.getUuid());

    return "fileresources/edit";
  }

  @GetMapping("/fileresources")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "fileresources/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "fileresources";
  }

  @GetMapping(value = "/fileresources/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FileResource resource = service.getByUuid(uuid);
    if (resource == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, resource.getLabel().getLocales());
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("fileresource", resource);
    return "fileresources/view";
  }
}
