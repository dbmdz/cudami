package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.CudamiWebsitesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for website management pages. */
@Controller
public class WebsitesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiWebsitesClient service;

  @Autowired
  public WebsitesController(
      LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forWebsites();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "websites";
  }

  @GetMapping("/websites/new")
  public String create(Model model) throws HttpException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "websites/create";
  }

  @GetMapping("/api/websites/new")
  @ResponseBody
  public Website create() {
    return service.create();
  }

  @GetMapping("/websites/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Website website = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, website.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("url", website.getUrl());
    model.addAttribute("uuid", website.getUuid());

    return "websites/edit";
  }

  @GetMapping("/api/websites")
  @ResponseBody
  public PageResponse<WebsiteImpl> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize);
    return service.find(pageRequest);
  }

  @GetMapping("/api/websites/{uuid}")
  @ResponseBody
  public Website get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/api/websites/{uuid}/webpages")
  @ResponseBody
  public PageResponse<Webpage> getRootpages(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    return service.getRootPages(uuid, new PageRequestImpl(pageNumber, pageSize));
  }

  @GetMapping("/websites")
  public String list(Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "websites/list";
  }

  @PostMapping("/api/websites/new")
  public ResponseEntity save(@RequestBody Website website) {
    try {
      Website websiteDb = service.save(website);
      return ResponseEntity.status(HttpStatus.CREATED).body(websiteDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save website: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/websites/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Website website) {
    try {
      Website websiteDb = service.update(uuid, website);
      return ResponseEntity.ok(websiteDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save website with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/websites/{uuid}/webpages")
  public ResponseEntity updateRootPagesOrder(
      @PathVariable UUID uuid, @RequestBody List<Webpage> rootPages) throws HttpException {
    boolean successful = service.updateRootPagesOrder(uuid, rootPages);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/websites/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Website website = (Website) service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, website.getLabel().getLocales());
    List<Locale> existingWebpageLanguages =
        website.getRootPages().stream()
            .flatMap(child -> child.getLabel().getLocales().stream())
            .collect(Collectors.toList());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute(
        "existingWebpageLanguages",
        languageSortingHelper.sortLanguages(displayLocale, existingWebpageLanguages));
    model.addAttribute("website", website);

    return "websites/view";
  }
}
