package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiHeadwordEntriesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IdentifiableController
    extends AbstractIdentifiablesController<Identifiable, CudamiIdentifiablesClient<Identifiable>> {

  CudamiHeadwordEntriesClient headwordEntriesService;

  public IdentifiableController(CudamiClient client, LanguageService languageService) {
    super(client.forIdentifiables(), languageService);
    this.headwordEntriesService = client.forHeadwordEntries();
  }

  public String doForward(Identifiable identifiable, Model model) throws TechnicalException {
    final String uuid = identifiable.getUuid().toString();
    IdentifiableObjectType identifiableObjectType = identifiable.getIdentifiableObjectType();
    switch (identifiableObjectType) {
      case ARTICLE:
        return "forward:/articles/" + uuid;
        // List<SubtopicImpl> subtopics =
        // subtopicService.getSubtopicsOfEntity(entity.getUuid());
        // if (!subtopics.isEmpty()) {
        // Subtopic subtopic = subtopics.get(0);
        // return "forward:/knowledge/node/" + subtopic.getUuid().toString();
        // }

      case COLLECTION:
        return "forward:/collections/" + uuid;
      case CORPORATE_BODY:
        return "forward:/corporatebodies/" + uuid;
      case DIGITAL_OBJECT:
        return "forward:/digitalobjects/" + uuid;
      case GEO_LOCATION:
        return "forward:/geolocations/" + uuid;
        // GeoLocation geoLocation = geoLocationsService.findOne(entity.getUuid());
        // GeoLocationType geoLocationType = geoLocation.getGeoLocationType();
        // switch (geoLocationType) {
        // case CANYON:
        // return "forward:/geo/canyons/" + uuid;
        // case CAVE:
        // return "forward:/geo/caves/" + uuid;
        // case CONTINENT:
        // return "forward:/geo/continents/" + uuid;
        // case COUNTRY:
        // return "forward:/geo/countries/" + uuid;
        // case CREEK:
        // return "forward:/geo/creeks/" + uuid;
        // case HUMAN_SETTLEMENT:
        // return "forward:/geo/human_settlements/" + uuid;
        // case LAKE:
        // return "forward:/geo/lakes/" + uuid;
        // case MOUNTAIN:
        // return "forward:/geo/mountains/" + uuid;
        // case OCEAN:
        // return "forward:/geo/oceans/" + uuid;
        // case RIVER:
        // return "forward:/geo/rivers/" + uuid;
        // case SEA:
        // return "forward:/geo/seas/" + uuid;
        // case STILL_WATERS:
        // return "forward:/geo/still_waters/" + uuid;
        // case VALLEY:
        // return "forward:/geo/valleys/" + uuid;
        // }
      case HEADWORD_ENTRY:
        HeadwordEntry headwordEntry = headwordEntriesService.getByUuid(identifiable.getUuid());
        UUID headwordUuid = headwordEntry.getHeadword().getUuid();
        return "redirect:/headwords/" + headwordUuid;
      case ITEM:
        return "forward:/items/" + uuid;
      case MANIFESTATION:
        return "forward:/manifestations/" + uuid;
      case PERSON:
        return "forward:/persons/" + uuid;
      case TOPIC:
        return "forward:/topics/" + uuid;
      case WORK:
        return "forward:/works/" + uuid;
      default:
        throw new TechnicalException(
            "Unhandled object type " + identifiable.getIdentifiableObjectType());
    }
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

  @GetMapping(value = {"/identifiables/{namespace:[a-zA-Z_\\-]+}:{id:.+}"})
  public String view(@PathVariable String namespace, @PathVariable String id, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Identifiable identifiable =
        ((CudamiIdentifiablesClient) service).getByIdentifier(namespace, id);
    if (identifiable == null) {
      throw new ResourceNotFoundException("get entity by identifier with " + namespace + ":" + id);
    }
    return doForward(identifiable, model);
  }
}
