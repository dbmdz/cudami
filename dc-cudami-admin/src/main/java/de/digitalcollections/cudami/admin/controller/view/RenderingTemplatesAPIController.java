package de.digitalcollections.cudami.admin.controller.view;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.view.CudamiRenderingTemplatesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.RenderingTemplate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "RenderingTemplates" endpoints (API). */
@RestController
public class RenderingTemplatesAPIController
    extends AbstractPagingAndSortingController<RenderingTemplate> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RenderingTemplatesAPIController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiRenderingTemplatesClient service;

  public RenderingTemplatesAPIController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forRenderingTemplates();
  }

  @GetMapping("/api/renderingtemplates/new")
  @ResponseBody
  public RenderingTemplate createModel() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/renderingtemplates")
  @ResponseBody
  public BTResponse<RenderingTemplate> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "name") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    PageResponse<RenderingTemplate> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public RenderingTemplate getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
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

  @PutMapping("/api/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
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
