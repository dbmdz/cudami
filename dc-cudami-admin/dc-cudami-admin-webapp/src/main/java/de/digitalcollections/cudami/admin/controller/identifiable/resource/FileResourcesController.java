package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.CudamiFileResourceService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for resource management pages. */
@Controller
public class FileResourcesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourcesController.class);

  LocaleRepository localeRepository;
  CudamiFileResourceService service;

  @Autowired
  public FileResourcesController(
      LocaleRepository localeRepository, CudamiFileResourceService service) {
    this.localeRepository = localeRepository;
    this.service = service;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "fileresources";
  }

  @GetMapping(value = "/fileresources/new")
  public String create(Model model) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    return "fileresources/create";
  }

  @GetMapping("/api/fileresources/new")
  @ResponseBody
  public FileResource create() {
    return service.create();
  }

  @GetMapping("/fileresources/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) {
    FileResource fileResource = service.get(uuid);
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("filename", fileResource.getFilename());
    model.addAttribute("uuid", fileResource.getUuid());
    return "fileresources/edit";
  }

  @GetMapping("/api/fileresources/{uuid}")
  @ResponseBody
  public FileResource get(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @GetMapping("/fileresources")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"label"},
              size = 25)
          Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/fileresources"));
    return "fileresources/list";
  }

  @PostMapping("/api/fileresources/new")
  public ResponseEntity save(@RequestBody FileResource fileResource)
      throws IdentifiableServiceException {
    FileResource fileResourceDb = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      fileResourceDb = service.save(fileResource);
      headers.setLocation(URI.create("/fileresources/" + fileResourceDb.getUuid().toString()));
    } catch (Exception e) {
      LOGGER.error("Cannot save fileresource: ", e);
      headers.setLocation(URI.create("/fileresources/new"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @PutMapping("/api/fileresources/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody FileResource fileResource)
      throws IdentifiableServiceException {
    HttpHeaders headers = new HttpHeaders();
    try {
      // get object from db
      FileResource fileResourceDb = service.get(uuid);
      // just update the fields, that were editable
      fileResourceDb.setLabel(fileResource.getLabel());
      fileResourceDb.setDescription(fileResource.getDescription());
      service.update(fileResourceDb);
      headers.setLocation(URI.create("/fileresources/" + uuid));
    } catch (Exception e) {
      String message = "Cannot save fileresource with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      headers.setLocation(URI.create("/fileresources/" + uuid + "/edit"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @PostMapping("/api/fileresources/new/upload")
  @ResponseBody
  public FileResource upload(HttpServletRequest request, RedirectAttributes redirectAttributes)
      throws InterruptedException, IOException {
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    if (!isMultipart) {
      // Inform user about invalid request
      redirectAttributes.addFlashAttribute("message", "Invalid file resource!");
      return null;
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

          FileResource fileResource = service.upload(stream, filename, contentType);
          return fileResource;
        }
      }
    } catch (IOException | FileUploadException e) {
      LOGGER.error("Error saving uploaded file data", e);
      redirectAttributes.addFlashAttribute("message", "Error saving file resource!");
      return null;
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
    LOGGER.warn("Invalid file resource!");
    return null;
  }

  @GetMapping(value = "/fileresources/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    FileResource resource = service.get(uuid);
    model.addAttribute("availableLanguages", resource.getLabel().getLocales());
    model.addAttribute("fileresource", resource);
    return "fileresources/view";
  }
}
