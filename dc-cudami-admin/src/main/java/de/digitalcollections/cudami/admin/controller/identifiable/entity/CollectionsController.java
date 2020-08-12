package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiCollectionsClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for collection management pages. */
@Controller
public class CollectionsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiCollectionsClient service;

  @Autowired
  public CollectionsController(
      LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forCollections();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "collections";
  }

  @PatchMapping("/api/collections/{uuid}/digitalobjects")
  public ResponseEntity addDigitalObjects(
      @PathVariable UUID uuid, @RequestBody List<DigitalObject> digitalObjects)
      throws HttpException {
    boolean successful = service.addDigitalObjects(uuid, digitalObjects);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/collections/new")
  public String create(
      Model model,
      @RequestParam(name = "parentType", required = false) String parentType,
      @RequestParam(name = "parentUuid", required = false) String parentUuid)
      throws HttpException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "collections/create";
  }

  @GetMapping("/api/collections/new")
  @ResponseBody
  public Collection create() {
    return service.create();
  }

  @GetMapping("/collections/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Collection collection = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, collection.getLabel().getLocales());

    model.addAttribute("activeLanguage", existingLanguages.get(0));
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", collection.getUuid());

    return "collections/edit";
  }

  @GetMapping("/api/collections/{uuid}")
  @ResponseBody
  public Collection get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/api/collections/{uuid}/digitalobjects")
  @ResponseBody
  public PageResponse<DigitalObject> getDigitalObjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    PageRequest pageRequest = new PageRequestImpl();
    pageRequest.setPageNumber(pageNumber);
    pageRequest.setPageSize(pageSize);
    return service.getDigitalObjects(uuid, pageRequest);
  }

  @GetMapping("/collections")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"lastModified"},
              direction = Sort.Direction.DESC,
              size = 25)
          Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.findTopCollections(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/collections"));
    return "collections/list";
  }

  @DeleteMapping("/api/collections/{collectionUuid}/digitalobjects/{digitalobjectUuid}")
  @ResponseBody
  public ResponseEntity removeDigitalObject(
      @PathVariable UUID collectionUuid, @PathVariable UUID digitalobjectUuid)
      throws HttpException {
    boolean successful = service.removeDigitalObject(collectionUuid, digitalobjectUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections/new")
  public ResponseEntity save(
      @RequestBody Collection collection,
      @RequestParam(name = "parentType", required = false) String parentType,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid) {
    try {
      Collection collectionDb = null;
      if ("collection".equals(parentType)) {
        collectionDb = service.saveWithParentCollection(collection, parentUuid);
      } else {
        collectionDb = service.save(collection);
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(collectionDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save collection: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/collections/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Collection collection) {
    try {
      Collection collectionDb = service.update(uuid, collection);
      return ResponseEntity.ok(collectionDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save collection with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/collections/{uuid}")
  public String view(
      @PathVariable UUID uuid, @PageableDefault(size = 25) Pageable pageable, Model model)
      throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Collection collection = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, collection.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("collection", collection);

    BreadcrumbNavigation breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);
    List<Node> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    model.addAttribute("breadcrumbs", breadcrumbs);

    return "collections/view";
  }
}
