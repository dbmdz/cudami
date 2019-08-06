package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.CudamiFileResourceService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
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
  LocaleService localeService;

  @Autowired
  CudamiFileResourceService cudamiFileResourceService;

  @Value(value = "${cudami.server.address}")
  private String cudamiServerAddress;

  @Autowired
  ObjectMapper objectMapper;

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

  // FIXME: add proper error and validation handling (using results and feedbackmessages instead "message" flash attribute)
  @PostMapping("/fileresources/new/upload")
  public String upload(HttpServletRequest request, RedirectAttributes redirectAttributes) throws InterruptedException, IOException {

    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    if (!isMultipart) {
      // Inform user about invalid request
      redirectAttributes.addFlashAttribute("message", "Invalid file resource!");
      return "redirect:/fileresources";
    }
    InputStream stream = null;
    try {
      ServletFileUpload upload = new ServletFileUpload();
      FileItemIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        if (!item.isFormField()) {
          String contentType = item.getContentType();
          String filename = item.getName();
          stream = item.openStream();

          FileResource fileResource = cudamiFileResourceService.upload(stream, filename, contentType);
          redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + filename + "!");
          redirectAttributes.addFlashAttribute("isNew", true);
          return "redirect:/fileresources/new/metadata/" + fileResource.getUuid().toString();
        }
      }
    } catch (IOException | FileUploadException e) {
      LOGGER.error("Error saving uploaded file data", e);
      redirectAttributes.addFlashAttribute("message", "Error saving file resource!");
      return "redirect:/fileresources";
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
    redirectAttributes.addFlashAttribute("message", "Invalid file resource!");
    return "redirect:/fileresources";
  }

  @GetMapping(value = "/fileresources/new/metadata/{uuid}")
  public String metadata(@PathVariable UUID uuid, Model model) {
    FileResource fileresource = cudamiFileResourceService.get(uuid);
    Locale defaultLocale = localeService.getDefaultLocale();
    List<Locale> locales = localeService.getSupportedLocales().stream()
      .filter(locale -> !(defaultLocale.equals(locale) || locale.getDisplayName().isEmpty()))
      .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
      .collect(Collectors.toList());

    model.addAttribute("defaultLocale", defaultLocale);
    model.addAttribute("fileresource", fileresource);
    model.addAttribute("locales", locales);
    return "fileresources/create_02-metadata";
  }

  @PostMapping(value = "/fileresources/new/metadata/{uuid}")
  public String metadata(@PathVariable UUID uuid, @ModelAttribute @Valid FileResourceImpl fileResource, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "fileresources/create_02-metadata";
    }

    try {
      // get object from db
      FileResource fileResourceDb = cudamiFileResourceService.get(uuid);
      // just update the fields, that were editable
      fileResourceDb.setLabel(fileResource.getLabel());
      fileResourceDb.setDescription(fileResource.getDescription());

      // TODO update license
      // ...
      cudamiFileResourceService.update(fileResourceDb);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save fileresource with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      redirectAttributes.addFlashAttribute("isNew", true);
      return "redirect:/fileresources/" + uuid + "/edit";
    }

    if (results.hasErrors()) {
      return "fileresources/create_02-metadata";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
//    return "redirect:/fileresources/new/" + fileResourceUuid + "/license";
    return "redirect:/fileresources/" + uuid;
  }

  @GetMapping(value = "/fileresources/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    FileResource resource = cudamiFileResourceService.get(uuid);
    model.addAttribute("availableLocales", resource.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefaultLocale());
    model.addAttribute("fileresource", resource);
    return "fileresources/view";
  }

  @GetMapping(value = "/fileresources/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    FileResource fileresource = cudamiFileResourceService.get(uuid);

    HashSet<Locale> availableLocales = (HashSet<Locale>) fileresource.getLabel().getLocales();
    Set<String> availableLocaleTags = availableLocales.stream().map(Locale::toLanguageTag).collect(Collectors.toSet());
    List<Locale> locales = localeService.getSupportedLocales().stream()
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

      cudamiFileResourceService.update(fileResourceDb);
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
