package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.CudamiWebsitesClient;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
  public String create(Model model) throws Exception {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "websites/create";
  }

  @GetMapping("/api/websites/new")
  @ResponseBody
  public Website create() {
    return service.create();
  }

  @GetMapping("/websites/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws Exception {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Website website = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, website.getLabel().getLocales());

    model.addAttribute("activeLanguage", existingLanguages.get(0));
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("url", website.getUrl());
    model.addAttribute("uuid", website.getUuid());

    return "websites/edit";
  }

  @GetMapping("/api/websites/{uuid}")
  @ResponseBody
  public Website get(@PathVariable UUID uuid) throws Exception {
    return service.findOne(uuid);
  }

  @GetMapping("/websites")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"email"},
              size = 25)
          Pageable pageable)
      throws Exception {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/websites"));
    return "websites/list";
  }

  @PostMapping("/api/websites/new")
  public ResponseEntity save(@RequestBody Website website) throws IdentifiableServiceException {
    try {
      Website websiteDb = service.save(website);
      return ResponseEntity.status(HttpStatus.CREATED).body(websiteDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save website: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/websites/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Website website)
      throws IdentifiableServiceException {
    try {
      Website websiteDb = service.update(uuid, website);
      return ResponseEntity.ok(websiteDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save website with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/websites/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws Exception {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Website website = (Website) service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, website.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("website", website);

    return "websites/view";
  }
}
