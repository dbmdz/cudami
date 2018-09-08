package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.file.business.api.FileResourceService;
import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.ResourceService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for resource management pages.
 */
@Controller
//@SessionAttributes(value = {"webpage"})
public class ResourcesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesController.class);

  private MessageSource messageSource;

  @Autowired
  FileResourceService fileResourceService;

  @Autowired
  LocaleService localeService;

  @Autowired
  ResourceService<Resource> resourceService;

  @Autowired
  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @GetMapping("/resources")
  public String list(Model model, @PageableDefault(sort = {"label"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = resourceService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/resources"));
    return "resources/list";
  }

  @PostMapping("/resources/new")
  public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    FileResource fileResource;
    try {
      String contentType = file.getContentType();
      final MimeType mimeType = MimeType.fromTypename(contentType);

      fileResource = fileResourceService.create(null, null, mimeType);

      long size = file.getSize();
      fileResource.setSizeInBytes(size);

      String originalFilename = file.getOriginalFilename();
      fileResource.setFilename(originalFilename);

      InputStream inputStream = file.getInputStream();
      fileResourceService.write(fileResource, inputStream);
    } catch (IOException ex) {
      LOGGER.error("Error reading uploaded file data", ex);
    }
    redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
    return "redirect:/resources";
  }
}
