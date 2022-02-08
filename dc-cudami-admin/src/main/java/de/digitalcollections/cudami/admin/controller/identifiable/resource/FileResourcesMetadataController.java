package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesMetadataClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
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

/** Controller for resource management pages. */
@Controller
public class FileResourcesMetadataController extends AbstractController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourcesMetadataController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiFileResourcesMetadataClient service;

  public FileResourcesMetadataController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forFileResourcesMetadata();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "fileresources";
  }

  @GetMapping(value = "/fileresources/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "fileresources/create";
  }

  @GetMapping("/api/fileresources/new")
  @ResponseBody
  public FileResource create() {
    return service.create();
  }

  @GetMapping("/fileresources/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FileResource fileResource = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, fileResource.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("filename", fileResource.getFilename());
    model.addAttribute("uuid", fileResource.getUuid());

    return "fileresources/edit";
  }

  @GetMapping("/api/fileresources")
  @ResponseBody
  public PageResponse<FileResource> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.find(searchPageRequest);
  }

  @GetMapping("/api/fileresources/{uuid}")
  @ResponseBody
  public FileResource get(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/fileresources")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "fileresources/list";
  }

  @PostMapping("/api/fileresources")
  public ResponseEntity save(@RequestBody FileResource fileResource) {
    try {
      FileResource fileResourceDb = service.save(fileResource);
      return ResponseEntity.status(HttpStatus.CREATED).body(fileResourceDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save fileresource: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/api/fileresources/type/{type}")
  @ResponseBody
  public SearchPageResponse<FileResource> searchFileResourcesByType(
      @PathVariable String type,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false) String sortField,
      @RequestParam(name = "sortDirection", required = false) Direction sortDirection,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    Sorting sorting = null;
    if (sortField != null && sortDirection != null) {
      Order order = new Order(sortDirection, sortField);
      sorting = new Sorting(order);
    }
    SearchPageRequest pageRequest =
        new SearchPageRequest(searchTerm, pageNumber, pageSize, sorting);
    return service.findFileResourcesByType(pageRequest, type);
  }

  @PutMapping("/api/fileresources/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody FileResource fileResource) {
    try {
      FileResource fileResourceDb = service.update(uuid, fileResource);
      return ResponseEntity.ok(fileResourceDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save fileresource with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping(value = "/fileresources/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FileResource resource = service.getByUuid(uuid);
    if (resource == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, resource.getLabel().getLocales());
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("fileresource", resource);
    return "fileresources/view";
  }
}
