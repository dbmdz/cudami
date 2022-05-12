package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiDigitalObjectsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for digital objects management pages. */
@Controller
public class DigitalObjectsController extends AbstractController {

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiDigitalObjectsClient service;

  public DigitalObjectsController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.service = client.forDigitalObjects();
  }

  @GetMapping({"/api/digitalobjects", "/api/digitalobjects/search"})
  @ResponseBody
  public PageResponse<DigitalObject> find(
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

  @GetMapping(
      value = "/api/digitalobjects/{uuid}/collections",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public PageResponse<Collection> getAssociatedCollections(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    return this.service.findCollections(uuid, pageRequest);
  }

  @GetMapping(
      value = "/api/digitalobjects/{uuid}/projects",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public PageResponse<Project> getAssociatedProjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    return this.service.findProjects(uuid, pageRequest);
  }

  @GetMapping("/api/digitalobjects/identifier/{namespace}:{id}")
  @ResponseBody
  public DigitalObject getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws TechnicalException {
    return service.getByIdentifier(namespace, id);
  }

  @GetMapping("/api/digitalobjects/{refId:[0-9]+}")
  @ResponseBody
  public DigitalObject getByRefId(@PathVariable long refId) throws TechnicalException {
    return service.getByRefId(refId);
  }

  @GetMapping(
      "/api/digitalobjects/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
  @ResponseBody
  public DigitalObject getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/digitalobjects")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "digitalobjects/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "digitalobjects";
  }

  @GetMapping("/digitalobjects/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    DigitalObject digitalObject = service.getByUuid(uuid);
    if (digitalObject == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingCollectionLanguages = this.service.getLanguagesOfCollections(uuid),
        existingProjectLanguages = this.service.getLanguagesOfProjects(uuid);

    model
        .addAttribute("digitalObject", digitalObject)
        .addAttribute("existingCollectionLanguages", existingCollectionLanguages)
        .addAttribute("existingProjectLanguages", existingProjectLanguages);
    return "digitalobjects/view";
  }
}
