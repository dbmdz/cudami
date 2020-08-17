package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiFileResourcesMetadataClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
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

  @Autowired
  public FileResourcesMetadataController(
      LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forFileResourcesMetadata();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "fileresources";
  }

  @GetMapping(value = "/fileresources/new")
  public String create(Model model) throws HttpException {
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
      throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FileResource fileResource = service.findOne(uuid);
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

  @GetMapping("/api/fileresources/{uuid}")
  @ResponseBody
  public FileResource get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/fileresources")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"filename"},
              direction = Sort.Direction.ASC,
              size = 25)
          Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/fileresources"));
    return "fileresources/list";
  }

  @PostMapping("/api/fileresources/new")
  public ResponseEntity save(@RequestBody FileResource fileResource) {
    try {
      FileResource fileResourceDb = service.save(fileResource);
      return ResponseEntity.status(HttpStatus.CREATED).body(fileResourceDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save fileresource: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/api/fileresources/images")
  @ResponseBody
  public SearchPageResponse<FileResourceImpl> searchImages(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "lastModified")
          String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC")
          Direction sortDirection,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws HttpException {
    Order order = new OrderImpl(sortDirection, sortField);
    Sorting sorting = new SortingImpl(order);
    SearchPageRequest pageRequest =
        new SearchPageRequestImpl(searchTerm, pageNumber, pageSize, sorting);
    return service.findImages(pageRequest);
  }

  @PutMapping("/api/fileresources/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody FileResource fileResource) {
    try {
      FileResource fileResourceDb = service.update(uuid, fileResource);
      return ResponseEntity.ok(fileResourceDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save fileresource with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping(value = "/fileresources/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FileResource resource = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, resource.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("fileresource", resource);

    return "fileresources/view";
  }
}
