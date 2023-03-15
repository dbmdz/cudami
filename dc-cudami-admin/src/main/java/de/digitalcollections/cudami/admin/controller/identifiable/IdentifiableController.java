package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiHeadwordEntriesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IdentifiableController extends AbstractController {

  CudamiHeadwordEntriesClient headwordEntriesService;
  CudamiIdentifiablesClient service;

  public IdentifiableController(CudamiClient client) {
    this.service = client.forIdentifiables();
    this.headwordEntriesService = client.forHeadwordEntries();
  }

  public String doForward(Identifiable identifiable, Model model) throws TechnicalException {
    final String uuid = identifiable.getUuid().toString();
    IdentifiableObjectType identifiableObjectType = identifiable.getIdentifiableObjectType();
    switch (identifiableObjectType) {
      case ARTICLE:
        return "forward:/articles/" + uuid;
        //        List<SubtopicImpl> subtopics =
        // subtopicService.getSubtopicsOfEntity(entity.getUuid());
        //        if (!subtopics.isEmpty()) {
        //          Subtopic subtopic = subtopics.get(0);
        //          return "forward:/knowledge/node/" + subtopic.getUuid().toString();
        //        }

      case COLLECTION:
        return "forward:/collections/" + uuid;
      case CORPORATE_BODY:
        return "forward:/corporatebodies/" + uuid;
      case DIGITAL_OBJECT:
        return "forward:/digitalobjects/" + uuid;
      case GEO_LOCATION:
        return "forward:/geolocations/" + uuid;
        //        GeoLocation geoLocation = geoLocationsService.findOne(entity.getUuid());
        //        GeoLocationType geoLocationType = geoLocation.getGeoLocationType();
        //        switch (geoLocationType) {
        //          case CANYON:
        //            return "forward:/geo/canyons/" + uuid;
        //          case CAVE:
        //            return "forward:/geo/caves/" + uuid;
        //          case CONTINENT:
        //            return "forward:/geo/continents/" + uuid;
        //          case COUNTRY:
        //            return "forward:/geo/countries/" + uuid;
        //          case CREEK:
        //            return "forward:/geo/creeks/" + uuid;
        //          case HUMAN_SETTLEMENT:
        //            return "forward:/geo/human_settlements/" + uuid;
        //          case LAKE:
        //            return "forward:/geo/lakes/" + uuid;
        //          case MOUNTAIN:
        //            return "forward:/geo/mountains/" + uuid;
        //          case OCEAN:
        //            return "forward:/geo/oceans/" + uuid;
        //          case RIVER:
        //            return "forward:/geo/rivers/" + uuid;
        //          case SEA:
        //            return "forward:/geo/seas/" + uuid;
        //          case STILL_WATERS:
        //            return "forward:/geo/still_waters/" + uuid;
        //          case VALLEY:
        //            return "forward:/geo/valleys/" + uuid;
        //        }
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
  public List<Identifiable> find(@RequestParam(name = "term") String searchTerm)
      throws TechnicalException {
    return service.find(searchTerm, 25);
  }

  @GetMapping(value = {"/identifiables/{namespace:[a-zA-Z_\\-]+}:{id:.+}"})
  public String view(@PathVariable String namespace, @PathVariable String id, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Identifiable identifiable = service.getByIdentifier(namespace, id);
    if (identifiable == null) {
      throw new ResourceNotFoundException("get entity by identifier with " + namespace + ":" + id);
    }
    return doForward(identifiable, model);
  }
}
