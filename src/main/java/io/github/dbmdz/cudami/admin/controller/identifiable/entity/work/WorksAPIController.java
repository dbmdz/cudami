package io.github.dbmdz.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiWorksClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.admin.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import io.github.dbmdz.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
import io.github.dbmdz.cudami.admin.model.bootstraptable.BTRequest;
import io.github.dbmdz.cudami.admin.model.bootstraptable.BTResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Works" endpoints (API). */
@RestController
public class WorksAPIController extends AbstractEntitiesController<Work, CudamiWorksClient> {

  public WorksAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forWorks(), client, languageService);
  }

  @SuppressFBWarnings
  @GetMapping("/api/works")
  @ResponseBody
  public BTResponse<Work> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        Work.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
  }

  /*
  Used in templates/works/view.html
  */
  @GetMapping("/api/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children")
  @ResponseBody
  public BTResponse<Work> findChildWorks(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Work.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
    PageResponse<Work> pageResponse = ((CudamiWorksClient) service).findChildren(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  /*
  Used in templates/works/view.html
  */
  @GetMapping("/api/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/manifestations")
  @ResponseBody
  public BTResponse<Manifestation> findManifestations(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Manifestation.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<Manifestation> pageResponse =
        ((CudamiWorksClient) service).findManifestations(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }
}
