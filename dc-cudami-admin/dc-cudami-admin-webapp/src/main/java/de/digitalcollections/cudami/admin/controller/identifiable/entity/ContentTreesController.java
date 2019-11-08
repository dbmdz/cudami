package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for content trees management pages. */
@Controller
public class ContentTreesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreesController.class);

  LanguageSortingHelper languageSortingHelper;
  LocaleRepository localeRepository;
  ContentTreeService service;

  @Autowired
  public ContentTreesController(
      LanguageSortingHelper languageSortingHelper,
      LocaleRepository localeRepository,
      ContentTreeService service) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeRepository = localeRepository;
    this.service = service;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "contenttrees";
  }

  @GetMapping("/contenttrees/new")
  public String create(Model model) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    return "contenttrees/create";
  }

  @GetMapping("/api/contenttrees/new")
  @ResponseBody
  public ContentTree create() {
    return service.create();
  }

  @RequestMapping(value = "/contenttrees/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model) {
    ContentTree contentTree = service.get(uuid);
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("uuid", contentTree.getUuid());
    return "contenttrees/edit";
  }

  @GetMapping("/api/contenttrees/{uuid}")
  @ResponseBody
  public ContentTree get(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @GetMapping("/contenttrees")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"lastModified"},
              size = 25)
          Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/contenttrees"));
    return "contenttrees/list";
  }

  @PostMapping("/api/contenttrees/new")
  public ResponseEntity save(@RequestBody ContentTree contentTree)
      throws IdentifiableServiceException {
    try {
      ContentTree contentTreeDb = service.save(contentTree);
      return ResponseEntity.status(HttpStatus.CREATED).body(contentTreeDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save content tree: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/contenttrees/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody ContentTree contentTree)
      throws IdentifiableServiceException {
    try {
      ContentTree contentTreeDb = service.update(contentTree);
      return ResponseEntity.ok(contentTreeDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save content tree with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/contenttrees/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    ContentTree contentTree = (ContentTree) service.get(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, contentTree.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("contentTree", contentTree);

    return "contenttrees/view";
  }
}
