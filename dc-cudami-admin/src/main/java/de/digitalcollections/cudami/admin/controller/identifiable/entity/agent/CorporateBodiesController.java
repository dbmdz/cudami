package de.digitalcollections.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiCorporateBodiesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
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

/** Controller for CorporateBody management pages. */
@Controller
public class CorporateBodiesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodiesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiCorporateBodiesClient service;

  public CorporateBodiesController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forCorporateBodies();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "corporatebodies";
  }

  @GetMapping("/corporatebodies/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "corporatebodies/create";
  }

  @GetMapping("/api/corporatebodies/new")
  @ResponseBody
  public CorporateBody create() {
    return service.create();
  }

  @GetMapping("/corporatebodies/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    CorporateBody corporateBody = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, corporateBody.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", corporateBody.getUuid());

    return "corporatebodies/edit";
  }

  @GetMapping("/api/corporatebodies")
  @ResponseBody
  public PageResponse<CorporateBody> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.find(searchPageRequest);
  }

  @GetMapping("/api/corporatebodies/{uuid}")
  @ResponseBody
  public CorporateBody get(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/corporatebodies")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "corporatebodies/list";
  }

  @PostMapping("/api/corporatebodies")
  public ResponseEntity save(@RequestBody CorporateBody corporateBody) {
    try {
      CorporateBody corporateBodyDb = service.save(corporateBody);
      return ResponseEntity.status(HttpStatus.CREATED).body(corporateBodyDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save corporate body: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/corporatebodies/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody CorporateBody corporateBody) {
    try {
      CorporateBody corporateBodyDb = service.update(uuid, corporateBody);
      return ResponseEntity.ok(corporateBodyDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save corporate body with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/corporatebodies/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    CorporateBody corporateBody = service.getByUuid(uuid);
    if (corporateBody == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, corporateBody.getLabel().getLocales());
    model
        .addAttribute("corporateBody", corporateBody)
        .addAttribute("existingLanguages", existingLanguages);
    return "corporatebodies/view";
  }
}
