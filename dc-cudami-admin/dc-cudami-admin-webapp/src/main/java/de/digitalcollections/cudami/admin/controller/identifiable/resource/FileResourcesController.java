package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.file.business.api.FileResourceService;
import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.CudamiFileResourceService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.enums.FileResourcePersistenceType;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for resource management pages.
 */
@Controller
@SessionAttributes(value = {"fileresource"})
public class FileResourcesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourcesController.class);

  private MessageSource messageSource;

  @Autowired
  FileResourceService fileResourceService;

  @Autowired
  LocaleService localeService;

  @Autowired
  CudamiFileResourceService<FileResource> cudamiFileResourceService;

  @Autowired
  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @GetMapping("/fileresources")
  public String list(Model model, @PageableDefault(sort = {"label"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = cudamiFileResourceService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/fileresources"));
    return "fileresources/list";
  }

  @GetMapping(value = "/fileresources/new")
  public String create(Model model) {
    return "fileresources/create_01-binary";
  }

  // FIXME: add proper error and validation handling (using results and feedbackmessages)
  @PostMapping("/fileresources/new/upload")
  public String upload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    FileResource managedFileResource;
    byte[] bytes;
    if (!file.isEmpty()) {
      try {
        String contentType = file.getContentType();
        final MimeType mimeType = MimeType.fromTypename(contentType);

        try {
          managedFileResource = fileResourceService.create(null, FileResourcePersistenceType.MANAGED, mimeType);
        } catch (ResourceIOException ex) {
          LOGGER.error("Error creating file resource", ex);
          redirectAttributes.addFlashAttribute("message", "Error creating file resource!");
          return "redirect:/fileresources";
        }
        managedFileResource.setMimeType(mimeType);
        LOGGER.info("mimetype = " + managedFileResource.getMimeType().getTypeName());

        long size = file.getSize();
        managedFileResource.setSizeInBytes(size);
        LOGGER.info("filesize = " + managedFileResource.getSizeInBytes());

        String originalFilename = file.getOriginalFilename();
        managedFileResource.setFilename(originalFilename);
        LOGGER.info("filename = " + managedFileResource.getFilename());

        // set label to originalfilename for now. can be changed in next step of user input
        managedFileResource.setLabel(new LocalizedTextImpl(localeService.getDefault(), originalFilename));

        bytes = file.getBytes();
        try {
          FileResource fileResource = cudamiFileResourceService.save(managedFileResource, bytes);
          redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + originalFilename + "!");
          redirectAttributes.addFlashAttribute("isNew", true);
          return "redirect:/fileresources/new/metadata/" + fileResource.getUuid().toString();
        } catch (IdentifiableServiceException ex) {
          LOGGER.error("Error saving uploaded file data", ex);
          redirectAttributes.addFlashAttribute("message", "Error saving file resource!");
          return "redirect:/fileresources";
        }
      } catch (IOException ex) {
        LOGGER.error("Error saving uploaded file data", ex);
      }
    }
    redirectAttributes.addFlashAttribute("message", "Invalid file resource!");
    return "redirect:/fileresources";
  }

  @GetMapping(value = "/fileresources/new/metadata/{uuid}")
  public String metadata(@PathVariable UUID uuid, Model model) {
    FileResource fileresource = cudamiFileResourceService.get(uuid);
    Locale defaultLocale = localeService.getDefault();
    List<Locale> locales = localeService.findAll().stream()
        .filter(locale -> !(defaultLocale.equals(locale) || locale.getDisplayName().isEmpty()))
        .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
        .collect(Collectors.toList());

    model.addAttribute("defaultLocale", defaultLocale);
    model.addAttribute("fileresource", fileresource);
    model.addAttribute("locales", locales);
    return "fileresources/create_02-metadata";
  }

  @PostMapping(value = "/fileresources/new/metadata")
  public String metadata(@ModelAttribute @Valid FileResourceImpl fileResource, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "fileresources/create_02-metadata";
    }

    UUID fileResourceUuid = fileResource.getUuid();

    try {
      // get object from db
      FileResource fileResourceDb = cudamiFileResourceService.get(fileResourceUuid);
      // just update the fields, that were editable
      fileResourceDb.setLabel(fileResource.getLabel());
      fileResourceDb.setDescription(fileResource.getDescription());

      cudamiFileResourceService.update(fileResourceDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save fileresource with uuid=" + fileResourceUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      redirectAttributes.addFlashAttribute("isNew", true);
      return "redirect:/fileresources/" + fileResourceUuid + "/edit";
    }

    if (results.hasErrors()) {
      return "fileresources/create_02-metadata";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
//    return "redirect:/fileresources/new/" + fileResourceUuid + "/license";
    return "redirect:/fileresources/" + fileResourceUuid;
  }

  @GetMapping(value = "/fileresources/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    FileResource resource = cudamiFileResourceService.get(uuid);
    model.addAttribute("availableLocales", resource.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("fileresource", resource);
    return "fileresources/view";
  }

  @GetMapping(value = "/fileresources/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    FileResource fileresource = cudamiFileResourceService.get(uuid);

    HashSet<Locale> availableLocales = (HashSet<Locale>) fileresource.getLabel().getLocales();
    Set<String> availableLocaleTags = availableLocales.stream().map(Locale::toLanguageTag).collect(Collectors.toSet());
    List<Locale> locales = localeService.findAll().stream()
        .filter(locale -> !(availableLocaleTags.contains(locale.toLanguageTag()) || locale.getDisplayName().isEmpty()))
        .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
        .collect(Collectors.toList());

    model.addAttribute("fileresource", fileresource);
    model.addAttribute("availableLocales", availableLocales);
    model.addAttribute("locales", locales);
    if (redirectAttributes.containsAttribute("isNew")) {
      return "fileresources/create_02-metadata";
    } else {
      return "fileresources/edit";
    }
  }

  @PostMapping(value = "/fileresources/{pathUuid}/edit")
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid FileResourceImpl fileResource, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "fileresources/edit";
    }

    try {
      // get object from db
      FileResource fileResourceDb = cudamiFileResourceService.get(pathUuid);
      // just update the fields, that were editable
      fileResourceDb.setLabel(fileResource.getLabel());
      fileResourceDb.setDescription(fileResource.getDescription());

      cudamiFileResourceService.update(fileResourceDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save fileresource with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/fileresources/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      return "fileresources/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/fileresources/" + pathUuid;
  }
}
