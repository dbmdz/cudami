package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.net.URI;
import java.util.UUID;
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

/**
 * Controller for website management pages.
 */
@Controller
public class WebsitesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesController.class);

  LocaleRepository localeRepository;
  WebsiteService service;

  @Autowired
  public WebsitesController(LocaleRepository localeRepository, WebsiteService service) {
    this.localeRepository = localeRepository;
    this.service = service;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "websites";
  }

  @GetMapping("/websites/new")
  public String create(Model model) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    return "websites/create";
  }

  @GetMapping("/api/websites/new")
  @ResponseBody
  public Website create() {
    return service.create();
  }

  @GetMapping("/websites/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) {
    Website website = service.get(uuid);
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("url", website.getUrl());
    model.addAttribute("uuid", website.getUuid());
    return "websites/edit";
  }

  @GetMapping("/api/websites/{uuid}")
  @ResponseBody
  public Website get(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @GetMapping("/websites")
  public String list(Model model, @PageableDefault(sort = {"email"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/websites"));
    return "websites/list";
  }

  @PostMapping("/api/websites/new")
  public ResponseEntity save(@RequestBody Website website) throws IdentifiableServiceException {
    Website websiteDb = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      websiteDb = service.save(website);
      headers.setLocation(URI.create("/websites/" + websiteDb.getUuid().toString()));
    } catch (Exception e) {
      LOGGER.error("Cannot save website: ", e);
      headers.setLocation(URI.create("/websites/new"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @PutMapping("/api/websites/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Website website) throws IdentifiableServiceException {
    HttpHeaders headers = new HttpHeaders();
    try {
      service.update(website);
      headers.setLocation(URI.create("/websites/" + uuid));
    } catch (Exception e) {
      String message = "Cannot save website with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      headers.setLocation(URI.create("/websites/" + uuid + "/edit"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @GetMapping("/websites/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    Website website = (Website) service.get(uuid);
    model.addAttribute("availableLanguages", website.getLabel().getLocales());
    model.addAttribute("website", website);

    return "websites/view";
  }
}
