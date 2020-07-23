package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.CudamiTopicsClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
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
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for topics management pages. */
@Controller
public class TopicsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiTopicsClient service;

  @Autowired
  public TopicsController(LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forTopics();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "topics";
  }

  @GetMapping("/topics/new")
  public String create(Model model) throws Exception {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "topics/create";
  }

  @GetMapping("/api/topics/new")
  @ResponseBody
  public Topic create() {
    return service.create();
  }

  @GetMapping("/topics/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Topic topic = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, topic.getLabel().getLocales());

    model.addAttribute("activeLanguage", existingLanguages.get(0));
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", topic.getUuid());

    return "topics/edit";
  }

  @GetMapping("/api/topics/{uuid}")
  @ResponseBody
  public Topic get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/topics")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"lastModified"},
              direction = Sort.Direction.DESC,
              size = 25)
          Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/topics"));
    return "topics/list";
  }

  @PostMapping("/api/topics/new")
  public ResponseEntity save(@RequestBody Topic topic) {
    try {
      Topic topicDb = service.save(topic);
      return ResponseEntity.status(HttpStatus.CREATED).body(topicDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save topic: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/topics/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Topic topic) {
    try {
      Topic topicDb = service.update(uuid, topic);
      return ResponseEntity.ok(topicDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save topic with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/topics/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Topic topic = (Topic) service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, topic.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("topic", topic);

    return "topics/view";
  }
}
