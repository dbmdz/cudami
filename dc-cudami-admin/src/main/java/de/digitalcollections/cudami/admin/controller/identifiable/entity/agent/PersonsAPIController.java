package de.digitalcollections.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiPersonsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/** Controller for all public "Persons" endpoints (API). */
@RestController
public class PersonsAPIController
    extends AbstractIdentifiablesController<Person, CudamiPersonsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonsAPIController.class);

  @Autowired
  public PersonsAPIController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forPersons(), languageSortingHelper, client.forLocales());
  }

  @GetMapping("/api/persons/new")
  @ResponseBody
  public Person create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/persons")
  @ResponseBody
  public BTResponse<Person> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    PageResponse<Person> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Person getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/persons")
  public ResponseEntity save(@RequestBody Person person) {
    try {
      Person personDb = service.save(person);
      return ResponseEntity.status(HttpStatus.CREATED).body(personDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save person: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Person person) {
    try {
      Person personDb = service.update(uuid, person);
      return ResponseEntity.ok(personDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save person with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
