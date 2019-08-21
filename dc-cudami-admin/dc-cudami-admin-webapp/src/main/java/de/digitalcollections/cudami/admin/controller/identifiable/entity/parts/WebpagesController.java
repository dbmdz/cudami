package de.digitalcollections.cudami.admin.controller.identifiable.entity.parts;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;
import javax.validation.Valid;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for webpage management pages.
 */
@Controller
public class WebpagesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpagesController.class);

  @Autowired
  LocaleService localeService;

  @Autowired
  WebpageService service;

  @ModelAttribute("menu")
  protected String module() {
    return "webpages";
  }

  @GetMapping("/webpages/new")
  public String create(Model model, @RequestParam("parentType") String parentType, @RequestParam("parentUuid") String parentUuid) {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "webpages/create";
  }

  @GetMapping("/api/webpages/new")
  @ResponseBody
  public Webpage create() {
    return (Webpage) service.create();
  }

  @GetMapping("/webpages/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) {
    Webpage webpage = (Webpage) service.get(uuid);
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    model.addAttribute("uuid", webpage.getUuid());
    return "webpages/edit";
  }

  @GetMapping("/api/webpages/{uuid}")
  @ResponseBody
  public Webpage get(@PathVariable UUID uuid) {
    return (Webpage) service.get(uuid);
  }

  @GetMapping("/webpages")
  public String list(Model model, @PageableDefault(sort = {"email"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/webpages"));
    return "webpages/list";
  }

  @PostMapping("/api/webpages/new")
  public ResponseEntity save(
      @RequestBody Webpage webpage,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") UUID parentUuid
  ) throws IdentifiableServiceException {
    Webpage webpageDb = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      if (parentType.equals("website")) {
        webpageDb = service.saveWithParentWebsite(webpage, parentUuid);
        headers.setLocation(URI.create("/webpages/" + webpageDb.getUuid().toString()));
      } else if (parentType.equals("webpage")) {
        webpageDb = service.saveWithParentWebpage(webpage, parentUuid);
        headers.setLocation(URI.create("/webpages/" + webpageDb.getUuid().toString()));
      }
    } catch (Exception e) {
      if (parentType.equals("website")) {
        LOGGER.error("Cannot save top-level webpage: ", e);
      } else if (parentType.equals("webpage")) {
        LOGGER.error("Cannot save webpage: ", e);
      }
      headers.setLocation(URI.create("/webpages/create"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @PutMapping("/api/webpages/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Webpage webpage) throws IdentifiableServiceException {
    HttpHeaders headers = new HttpHeaders();
    try {
      service.update(webpage);
      headers.setLocation(URI.create("/webpages/" + uuid));
    } catch (Exception e) {
      String message = "Cannot save webpage with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      headers.setLocation(URI.create("/webpages/" + uuid + "/edit"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @GetMapping("/webpages/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    Webpage webpage = (Webpage) service.get(uuid);
    model.addAttribute("availableLocales", webpage.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefaultLocale());
    model.addAttribute("webpage", webpage);

    LinkedHashSet<FileResource> relatedFileResources = service.getRelatedFileResources(webpage);
    model.addAttribute("relatedFileResources", relatedFileResources);

    return "webpages/view";
  }

  @RequestMapping(value = "/webpages/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid WebpageImpl webpage, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes,
                       @RequestParam("parentType") String parentType,
                       @RequestParam("parentUuid") UUID parentUuid) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "webpages/create";
    }
    Webpage webpageDb = null;
    try {
      if (Objects.equals(parentType, "website")) {
        webpageDb = service.saveWithParentWebsite(webpage, parentUuid);
        LOGGER.info("Successfully saved top-level webpage");
      } else if (Objects.equals(parentType, "webpage")) {
        webpageDb = service.saveWithParentWebpage(webpage, parentUuid);
        LOGGER.info("Successfully saved webpage");
      }
    } catch (Exception e) {
      if (Objects.equals(parentType, "website")) {
        LOGGER.info("Cannot save top-level webpage: ", e);
      } else if (Objects.equals(parentType, "webpage")) {
        LOGGER.error("Cannot save webpage: ", e);
      }
      String message = ""; //messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    if (results.hasErrors()) {
      return "webpages/create";
    }
    status.setComplete();
    String message = ""; //messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/webpages/" + webpageDb.getUuid().toString();
  }

  @RequestMapping(value = "/webpages/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid WebpageImpl webpage, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "webpages/edit";
    }

    try {
      // get webpage from db
      Webpage webpageDb = (Webpage) service.get(pathUuid);
      // just update the fields, that were editable
      webpageDb.setLabel(webpage.getLabel());
      webpageDb.setDescription(webpage.getDescription());
      webpageDb.setText(webpage.getText());

      service.update(webpageDb);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save webpage with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/webpages/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      return "webpages/edit";
    }
    status.setComplete();
    String message = ""; //messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/webpages/" + pathUuid;
  }

}
