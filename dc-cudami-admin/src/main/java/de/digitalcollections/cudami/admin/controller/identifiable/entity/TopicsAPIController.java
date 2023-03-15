package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTRequest;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiTopicsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageResponse;
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

/** Controller for all public "Topics" endpoints (API). */
@RestController
public class TopicsAPIController extends AbstractEntitiesController<Topic, CudamiTopicsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicsAPIController.class);

  public TopicsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forTopics(), languageService);
  }

  @GetMapping("/api/topics/new")
  @ResponseBody
  public Topic create() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/api/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities")
  @ResponseBody
  public BTResponse<Entity> findRelatedEntities(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Entity.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<Entity> pageResponse =
        ((CudamiTopicsClient) service).findEntities(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources")
  @ResponseBody
  public BTResponse<FileResource> findRelatedFileResources(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            FileResource.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<FileResource> pageResponse =
        ((CudamiTopicsClient) service).findFileResources(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/topics")
  @ResponseBody
  public BTResponse<Topic> findSubtopic(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Topic.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
    PageResponse<Topic> pageResponse =
        ((CudamiTopicsClient) service).findSubtopics(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  @SuppressFBWarnings
  @GetMapping("/api/topics")
  @ResponseBody
  public BTResponse<Topic> findTop(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Topic.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
    PageResponse<Topic> pageResponse = ((CudamiTopicsClient) service).findTopTopics(btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Topic getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/topics")
  public ResponseEntity save(
      @RequestBody Topic topic,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid) {
    try {
      Topic topicDb = null;
      if (parentUuid == null) {
        topicDb = service.save(topic);
      } else {
        topicDb = ((CudamiTopicsClient) service).saveWithParentTopic(topic, parentUuid);
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(topicDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save topic: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Topic topic) {
    try {
      Topic topicDb = service.update(uuid, topic);
      return ResponseEntity.ok(topicDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save topic with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
