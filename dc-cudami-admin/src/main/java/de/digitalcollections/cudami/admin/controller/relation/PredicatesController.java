package de.digitalcollections.cudami.admin.controller.relation;

import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.relation.CudamiPredicatesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for predicate management pages. */
@Controller
public class PredicatesController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredicatesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiPredicatesClient service;

  public PredicatesController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forPredicates();
  }

  @GetMapping("/predicates/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "predicates/create";
  }

  @GetMapping("/api/predicates/new")
  @ResponseBody
  public Predicate createModel() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/predicates/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    Predicate predicate = service.getByUuid(uuid);

    List<Locale> existingLanguages = List.of(localeService.getDefaultLanguage());
    LocalizedText label = predicate.getLabel();
    if (!CollectionUtils.isEmpty(label)) {
      Locale displayLocale = LocaleContextHolder.getLocale();
      existingLanguages =
          languageSortingHelper.sortLanguages(displayLocale, predicate.getLabel().getLocales());
    }

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("value", predicate.getValue());
    model.addAttribute("uuid", predicate.getUuid());

    return "predicates/edit";
  }

  @GetMapping("/api/predicates")
  @ResponseBody
  public BTResponse<Predicate> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      HttpServletRequest request)
      throws TechnicalException {
    Map<String, String[]> parameterMap = request.getParameterMap();
    PageRequest pageRequest = new PageRequest(searchTerm, (int) Math.ceil(offset / limit), limit);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse<Predicate> pageResponse = service.find(pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/predicates/{uuid}")
  @ResponseBody
  public Predicate getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/predicates")
  public String list(Model model) throws TechnicalException {
    Locale locale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages", languageSortingHelper.sortLanguages(locale, service.getLanguages()));
    return "predicates/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "predicates";
  }

  @PostMapping("/api/predicates")
  public ResponseEntity save(@RequestBody Predicate predicate) {
    try {
      Predicate predicateDB = service.save(predicate);
      return ResponseEntity.status(HttpStatus.CREATED).body(predicateDB);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save predicate: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/predicates/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Predicate predicate) {
    try {
      Predicate predicateDB = service.update(uuid, predicate);
      return ResponseEntity.ok(predicateDB);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot update predicate with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/predicates/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Predicate predicate = service.getByUuid(uuid);
    if (predicate == null) {
      throw new ResourceNotFoundException();
    }

    List<Locale> existingLanguages = Collections.emptyList();
    LocalizedText label = predicate.getLabel();
    if (!CollectionUtils.isEmpty(label)) {
      Locale displayLocale = LocaleContextHolder.getLocale();
      existingLanguages =
          languageSortingHelper.sortLanguages(displayLocale, predicate.getLabel().getLocales());
    }

    model
        .addAttribute("predicate", predicate)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("value", predicate.getValue());
    return "predicates/view";
  }
}
