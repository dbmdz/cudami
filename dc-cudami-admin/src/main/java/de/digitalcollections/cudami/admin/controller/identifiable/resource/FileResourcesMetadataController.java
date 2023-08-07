package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.client.CudamiClient;
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
public class FileResourcesMetadataController
    extends AbstractIdentifiablesController<FileResource, CudamiFileResourcesMetadataClient> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourcesMetadataController.class);

  public FileResourcesMetadataController(CudamiClient client, LanguageService languageService) {
    super(client.forFileResourcesMetadata(), client, languageService);
  }

  @GetMapping(value = "/fileresources/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "fileresources/create";
  }

  @GetMapping("/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FileResource fileResource = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, fileResource.getLabel().getLocales());

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
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "fileresources/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "fileresources";
  }

  @GetMapping(value = "/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    FileResource resource = service.getByUuid(uuid);
    if (resource == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("fileresource", resource);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(resource);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "fileresources/view";
  }
}
