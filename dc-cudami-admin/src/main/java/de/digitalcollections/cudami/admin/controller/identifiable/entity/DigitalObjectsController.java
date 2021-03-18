package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiDigitalObjectsClient;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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

  private final CudamiDigitalObjectsClient service;
  private final CudamiLocalesClient localeService;
  private final LanguageSortingHelper languageSortingHelper;

  @Autowired
  public DigitalObjectsController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forDigitalObjects();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "digitalobjects";
  }

  @GetMapping("/api/digitalobjects/identifier/{namespace}:{id}")
  @ResponseBody
  public DigitalObject findOneByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws HttpException {
    return service.findOneByIdentifier(namespace, id);
  }

  @GetMapping("/digitalobjects")
  public String list(Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "digitalobjects/list";
  }

  @GetMapping("/api/digitalobjects")
  @ResponseBody
  public PageResponse<DigitalObject> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws HttpException {

    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.find(searchPageRequest);
  }

  @GetMapping("/api/digitalobjects/search")
  @ResponseBody
  public SearchPageResponse<DigitalObject> search(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false) String sortField,
      @RequestParam(name = "sortDirection", required = false) Direction sortDirection,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws HttpException {
    Sorting sorting = null;
    if (sortField != null && sortDirection != null) {
      Order order = new Order(sortDirection, sortField);
      sorting = new Sorting(order);
    }
    SearchPageRequest pageRequest =
        new SearchPageRequest(searchTerm, pageNumber, pageSize, sorting);
    return service.find(pageRequest);
  }

  @GetMapping("/digitalobjects/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    DigitalObject digitalObject = service.findOne(uuid);
    model.addAttribute("digitalObject", digitalObject);

    final PageResponse<Collection> pageResponseCollections =
        service.getCollections(uuid, new PageRequest(0, 100));
    List<Collection> collections = pageResponseCollections.getContent();
    model.addAttribute("collections", collections);

    final PageResponse<Project> pageResponseProjects =
        service.getProjects(uuid, new PageRequest(0, 100));
    List<Project> projects = pageResponseProjects.getContent();
    model.addAttribute("projects", projects);

    return "digitalobjects/view";
  }
}
