package de.digitalcollections.cudami.admin.controller.legal;

import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.legal.CudamiLicensesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/** Controller for license management pages. */
@Controller
public class LicensesController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicensesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiLicensesClient service;

  public LicensesController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forLicenses();
  }

  @GetMapping("/licenses/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "licenses/create";
  }

  @GetMapping("/api/licenses/new")
  @ResponseBody
  public License createModel() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/licenses/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    License license = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, license.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("url", license.getUrl());
    model.addAttribute("uuid", license.getUuid());

    return "licenses/edit";
  }

  @GetMapping("/api/licenses")
  @ResponseBody
  public PageResponse<License> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping("/api/licenses/{uuid}")
  @ResponseBody
  public License getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/licenses")
  public String list(Model model) throws TechnicalException {
    Locale locale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages", languageSortingHelper.sortLanguages(locale, service.getLanguages()));
    return "licenses/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "licenses";
  }

  @PostMapping("/api/licenses")
  public ResponseEntity save(@RequestBody License license) {
    try {
      License licenseDb = service.save(license);
      return ResponseEntity.status(HttpStatus.CREATED).body(licenseDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save license: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/licenses/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody License license) {
    try {
      License licenseDb = service.update(uuid, license);
      return ResponseEntity.ok(licenseDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save license with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/licenses/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    License license = service.getByUuid(uuid);
    if (license == null) {
      throw new ResourceNotFoundException();
    }
    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, license.getLabel().getLocales());
    model
        .addAttribute("license", license)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("url", license.getUrl());
    return "licenses/view";
  }
}
