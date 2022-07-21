package de.digitalcollections.cudami.admin.controller.view;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.view.CudamiRenderingTemplatesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.view.RenderingTemplate;
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

/** Controller for rendering template management pages. */
@Controller
public class RenderingTemplatesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderingTemplatesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiRenderingTemplatesClient service;

  public RenderingTemplatesController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.service = client.forRenderingTemplates();
  }

  @GetMapping("/renderingtemplates/new")
  public String create() {
    return "renderingtemplates/create";
  }

  @GetMapping("/api/renderingtemplates/new")
  @ResponseBody
  public RenderingTemplate createModel() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/renderingtemplates/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws TechnicalException {
    RenderingTemplate template = service.getByUuid(uuid);
    model.addAttribute("name", template.getName());
    model.addAttribute("uuid", template.getUuid());
    return "renderingtemplates/edit";
  }

  @GetMapping("/api/renderingtemplates")
  @ResponseBody
  public PageResponse<RenderingTemplate> find(
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

  @GetMapping("/api/renderingtemplates/{uuid}")
  @ResponseBody
  public RenderingTemplate getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/renderingtemplates")
  public String list(Model model) throws TechnicalException {
    Locale locale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        this.languageSortingHelper.sortLanguages(locale, this.service.getLanguages()));
    return "renderingtemplates/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "renderingtemplates";
  }

  @PostMapping("/api/renderingtemplates")
  public ResponseEntity save(@RequestBody RenderingTemplate template) {
    try {
      RenderingTemplate templateDb = service.save(template);
      return ResponseEntity.status(HttpStatus.CREATED).body(templateDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save rendering template: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/renderingtemplates/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody RenderingTemplate template) {
    try {
      RenderingTemplate templateDb = service.update(uuid, template);
      return ResponseEntity.ok(templateDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot update rendering template with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
