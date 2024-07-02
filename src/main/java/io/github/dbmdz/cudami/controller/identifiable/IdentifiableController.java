package io.github.dbmdz.cudami.controller.identifiable;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IdentifiableController
    extends AbstractIdentifiablesController<Identifiable, CudamiIdentifiablesClient<Identifiable>> {

  public IdentifiableController(CudamiClient client, LanguageService languageService) {
    super(client.forIdentifiables(), client, languageService);
  }

  @GetMapping(value = "/identifiables")
  @ResponseBody
  public List<Identifiable> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchField", required = false, defaultValue = "label")
          String searchField,
      @RequestParam(name = "term", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    // TODO: find code using "term" instead "searchTerm" and change it to "searchTerm"
    String dataLanguage = null;
    PageRequest pageRequest =
        createPageRequest(
            Identifiable.class,
            pageNumber,
            pageSize,
            sortBy,
            searchField,
            searchTerm,
            dataLanguage);
    PageResponse<Identifiable> response = service.find(pageRequest);
    return response.getContent();
  }

  @GetMapping(value = "/identifiables/search")
  public String search(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchField", required = false, defaultValue = "label")
          String searchField,
      @RequestParam(name = "term", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      Model model)
      throws TechnicalException {
    // TODO: find code using "term" instead "searchTerm" and change it to "searchTerm"
    model.addAttribute("search", searchTerm);
    return "identifiables/list";
  }

  @GetMapping(value = {"/identifiables/{namespace:[a-zA-Z\\d_\\-]+}:{id:.+}"})
  public String view(@PathVariable String namespace, @PathVariable String id, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Identifiable identifiable =
        ((CudamiIdentifiablesClient) service).getByIdentifier(namespace, id);
    if (identifiable == null) {
      return "/error/404";
    }
    return doRedirect(identifiable, model);
  }

  @GetMapping(value = {"/identifiables/{base64:[^:]+}"})
  public String viewBase64Encoded(@PathVariable String base64, Model model)
      throws TechnicalException, ResourceNotFoundException {
    String paramString = new String(Base64.decodeBase64(base64), StandardCharsets.UTF_8);
    Pattern identifierParamPattern = Pattern.compile("^([^:]+?):(.*)$");
    Matcher identifierParamMatcher = identifierParamPattern.matcher(paramString);
    if (!identifierParamMatcher.matches()) {
      return "/error/404";
    }
    String namespace = identifierParamMatcher.group(1);
    String id = identifierParamMatcher.group(2);

    Identifiable identifiable =
        ((CudamiIdentifiablesClient) service).getByIdentifier(namespace, id);
    if (identifiable == null) {
      return "/error/404";
    }
    return doRedirect(identifiable, model);
  }

  @GetMapping(value = {"/identifiables/uuid/{uuid:" + ParameterHelper.UUID_PATTERN + "}"})
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    UniqueObject identifiable = ((CudamiIdentifiablesClient) service).getByUuid(uuid);
    if (identifiable == null || !(identifiable instanceof Identifiable)) {
      throw new ResourceNotFoundException("get identifiable by uuid=" + uuid);
    }
    return doRedirect((Identifiable) identifiable, model);
  }
}
