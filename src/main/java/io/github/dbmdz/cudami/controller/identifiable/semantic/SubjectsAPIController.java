package io.github.dbmdz.cudami.controller.identifiable.semantic;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.semantic.CudamiSubjectsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.semantic.Subject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.identifiable.AbstractIdentifiablesController;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Subjects" endpoints (API). */
@RestController
public class SubjectsAPIController
    extends AbstractIdentifiablesController<Subject, CudamiSubjectsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubjectsAPIController.class);

  public SubjectsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forSubjects(), client, languageService);
  }

  @SuppressFBWarnings
  @GetMapping("/api/subjects")
  @ResponseBody
  public BTResponse<Subject> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "url") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        Subject.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
  }
}
