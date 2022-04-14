package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiCollectionsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

  public CollectionsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forCollections();
  }

  @PostMapping("/api/collections/{uuid}/digitalobjects")
  public ResponseEntity addDigitalObjects(
      @PathVariable UUID uuid, @RequestBody List<DigitalObject> digitalObjects)
      throws TechnicalException {
    boolean successful = service.addDigitalObjects(uuid, digitalObjects);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections/{collectionUuid}/subcollections/{subcollectionUuid}")
  public ResponseEntity addSubcollection(
      @PathVariable UUID collectionUuid, @PathVariable UUID subcollectionUuid)
      throws TechnicalException {
    boolean successful = service.addSubcollection(collectionUuid, subcollectionUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections/{collectionUuid}/subcollections")
  public ResponseEntity addSubcollections(
      @PathVariable UUID collectionUuid, @RequestBody List<Collection> subcollections)
      throws TechnicalException {
    boolean successful = service.addSubcollections(collectionUuid, subcollections);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @GetMapping({"/collections/new", "/subcollections/new"})
  public String create(
      Model model,
      @RequestParam(name = "parentType", required = false) String parentType,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid)
      throws TechnicalException {
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

  @GetMapping({"/collections/{uuid}/edit", "/subcollections/{uuid}/edit"})
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Collection collection = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, collection.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", collection.getUuid());

    return "collections/edit";
  }

  @GetMapping("/api/collections")
  @ResponseBody
  public SearchPageResponse<Collection> findTop(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return service.findTopCollections(searchPageRequest);
  }

  @GetMapping("/api/collections/{uuid}/digitalobjects")
  @ResponseBody
  public PageResponse<DigitalObject> findDigitalObjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.findDigitalObjects(uuid, searchPageRequest);
  }

  @GetMapping("/api/collections/{uuid}/subcollections")
  @ResponseBody
  public PageResponse<Collection> findSubcollections(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.findSubcollections(uuid, searchPageRequest);
  }

  @GetMapping({
    "/api/collections/identifier/{namespace}:{id}",
    "/api/subcollections/identifier/{namespace}:{id}"
  })
  @ResponseBody
  public Collection getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws TechnicalException {
    return service.getByIdentifier(namespace, id);
  }

  @GetMapping({"/api/collections/{refId:[0-9]+}", "/api/subcollections/{refId:[0-9]+}"})
  @ResponseBody
  public Collection getByRefId(@PathVariable long refId) throws TechnicalException {
    return service.getByRefId(refId);
  }

  @GetMapping({
    "/api/collections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
    "/api/subcollections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
  })
  @ResponseBody
  public Collection getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/collections")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(
            displayLocale, service.findLanguagesOfTopCollections()));
    return "collections/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "collections";
  }

  @DeleteMapping("/api/collections/{collectionUuid}/digitalobjects/{digitalobjectUuid}")
  @ResponseBody
  public ResponseEntity removeDigitalObject(
      @PathVariable UUID collectionUuid, @PathVariable UUID digitalobjectUuid)
      throws TechnicalException {
    boolean successful = service.removeDigitalObject(collectionUuid, digitalobjectUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/api/collections/{collectionUuid}/subcollections/{subcollectionUuid}")
  public ResponseEntity removeSubcollection(
      @PathVariable UUID collectionUuid, @PathVariable UUID subcollectionUuid)
      throws TechnicalException {
    boolean successful = service.removeSubcollection(collectionUuid, subcollectionUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections")
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
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save collection: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping({"/api/collections/search", "/api/subcollections/search"})
  @ResponseBody
  public SearchPageResponse<Collection> search(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return service.find(searchPageRequest);
  }

  @PutMapping("/api/collections/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Collection collection) {
    try {
      Collection collectionDb = service.update(uuid, collection);
      return ResponseEntity.ok(collectionDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save collection with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping({
    "/collections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
    "/subcollections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
  })
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Collection collection = service.getByUuid(uuid);
    if (collection == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, collection.getLabel().getLocales());
    List<Locale> existingSubcollectionLanguages =
        collection.getChildren().stream()
            .flatMap(child -> child.getLabel().getLocales().stream())
            .collect(Collectors.toList());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute(
        "existingSubcollectionLanguages",
        languageSortingHelper.sortLanguages(displayLocale, existingSubcollectionLanguages));
    model.addAttribute("collection", collection);

    List<Collection> parents = service.findParents(uuid);
    model.addAttribute("parents", parents);

    BreadcrumbNavigation breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);
    List<BreadcrumbNode> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    model.addAttribute("breadcrumbs", breadcrumbs);

    return "collections/view";
  }

  @GetMapping({"/collections/{refId:[0-9]+}", "/subcollections/{refId:[0-9]+}"})
  public String viewByRefId(@PathVariable long refId, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Collection collection = service.getByRefId(refId);
    if (collection == null) {
      throw new ResourceNotFoundException();
    }
    return view(collection.getUuid(), model);
  }
}
