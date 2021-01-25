package de.digitalcollections.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.entity.agent.CudamiCorporateBodiesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for CorporateBody management pages. */
@Controller
public class CorporateBodiesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodiesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiCorporateBodiesClient service;

  @Autowired
  public CorporateBodiesController(
      LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forCorporateBodies();
  }

  @GetMapping("/corporatebodies/new")
  public String create(Model model) throws HttpException {
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
      throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    CorporateBody corporateBody = service.findOne(uuid);
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

  @GetMapping("/api/corporatebodies/{uuid}")
  @ResponseBody
  public CorporateBody get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/corporatebodies")
  public String list(Model model, @PageableDefault(size = 25) Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/corporatebodies"));
    return "corporatebodies/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "corporatebodies";
  }

  @PostMapping("/api/corporatebodies/new")
  public ResponseEntity save(@RequestBody CorporateBody corporateBody) {
    try {
      CorporateBody corporateBodyDb = service.save(corporateBody);
      return ResponseEntity.status(HttpStatus.CREATED).body(corporateBodyDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save corporate body: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/corporatebodies/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody CorporateBody corporateBody) {
    try {
      CorporateBody corporateBodyDb = service.update(uuid, corporateBody);
      return ResponseEntity.ok(corporateBodyDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save corporate body with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/corporatebodies/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    CorporateBody corporateBody = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, corporateBody.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("corporateBody", corporateBody);

    return "corporatebodies/view";
  }
}
