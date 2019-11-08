package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiCorporationsClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
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

/** Controller for corporation management pages. */
@Controller
public class CorporationsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporationsController.class);

  LanguageSortingHelper languageSortingHelper;
  LocaleRepository localeRepository;
  CudamiCorporationsClient cudamiCorporationsClient;

  @Autowired
  public CorporationsController(
      LanguageSortingHelper languageSortingHelper,
      LocaleRepository localeRepository,
      CudamiCorporationsClient cudamiCorporationsClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeRepository = localeRepository;
    this.cudamiCorporationsClient = cudamiCorporationsClient;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "corporations";
  }

  @GetMapping("/corporations/new")
  public String create(Model model) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    return "corporations/create";
  }

  @GetMapping("/api/corporations/new")
  @ResponseBody
  public Corporation create() {
    return cudamiCorporationsClient.createCorporation();
  }

  @GetMapping("/corporations/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws HttpException {
    Corporation corporation = cudamiCorporationsClient.getCorporation(uuid);
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("uuid", corporation.getUuid());
    return "corporations/edit";
  }

  @GetMapping("/api/corporations/{uuid}")
  @ResponseBody
  public Corporation get(@PathVariable UUID uuid) throws HttpException {
    return cudamiCorporationsClient.getCorporation(uuid);
  }

  @GetMapping("/corporations")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"label"},
              size = 25)
          Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = cudamiCorporationsClient.findCorporations(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/corporations"));
    return "corporations/list";
  }

  @PostMapping("/api/corporations/new")
  public ResponseEntity save(@RequestBody Corporation corporation)
      throws IdentifiableServiceException {
    try {
      Corporation corporationDb = cudamiCorporationsClient.saveCorporation(corporation);
      return ResponseEntity.status(HttpStatus.CREATED).body(corporationDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save corporation: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/corporations/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Corporation corporation)
      throws IdentifiableServiceException {
    try {
      Corporation corporationDb = cudamiCorporationsClient.updateCorporation(corporation);
      return ResponseEntity.ok(corporationDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save corporation with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/corporations/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Corporation corporation = cudamiCorporationsClient.getCorporation(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, corporation.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("corporation", corporation);

    return "corporations/view";
  }
}
