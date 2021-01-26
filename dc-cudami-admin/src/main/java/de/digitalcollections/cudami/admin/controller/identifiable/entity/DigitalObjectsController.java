package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiDigitalObjectsClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

  @Autowired
  public DigitalObjectsController(CudamiClient client) {
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
  public String list(Model model, @PageableDefault(size = 25) Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/digitalobjects"));
    return "digitalobjects/list";
  }

  @GetMapping("/api/digitalobjects/search")
  @ResponseBody
  public SearchPageResponse<DigitalObjectImpl> search(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false) String sortField,
      @RequestParam(name = "sortDirection", required = false) Direction sortDirection,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws HttpException {
    Sorting sorting = null;
    if (sortField != null && sortDirection != null) {
      OrderImpl order = new OrderImpl(sortDirection, sortField);
      sorting = new SortingImpl(order);
    }
    SearchPageRequest pageRequest =
        new SearchPageRequestImpl(searchTerm, pageNumber, pageSize, sorting);
    return service.find(pageRequest);
  }

  @GetMapping("/digitalobjects/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    DigitalObject digitalObject = service.findOne(uuid);
    model.addAttribute("digitalObject", digitalObject);

    final PageResponse<Collection> pageResponseCollections =
        service.getCollections(uuid, new PageRequestImpl(0, 100));
    List<Collection> collections = pageResponseCollections.getContent();
    model.addAttribute("collections", collections);

    final PageResponse<Project> pageResponseProjects =
        service.getProjects(uuid, new PageRequestImpl(0, 100));
    List<Project> projects = pageResponseProjects.getContent();
    model.addAttribute("projects", projects);

    return "digitalobjects/view";
  }
}
