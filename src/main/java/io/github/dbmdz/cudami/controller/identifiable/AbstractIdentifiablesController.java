package io.github.dbmdz.cudami.controller.identifiable;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;

public class AbstractIdentifiablesController<
        I extends Identifiable, C extends CudamiIdentifiablesClient<I>>
    extends AbstractUniqueObjectController<I> {

  private final CudamiClient cudamiClient;

  protected AbstractIdentifiablesController(
      C service, CudamiClient cudamiClient, LanguageService languageService) {
    super(service, languageService);
    this.cudamiClient = cudamiClient;
  }

  protected List<Locale> getExistingLanguagesFromIdentifiable(Identifiable identifiable) {
    return getExistingLanguagesFromIdentifiables(List.of(identifiable));
  }

  protected List<Locale> getExistingLanguagesFromIdentifiables(
      List<? extends Identifiable> identifiables) {
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(identifiables)) {
      existingLanguages =
          identifiables.stream()
              .flatMap(
                  child ->
                      child.getLabel() != null
                          ? Stream.concat(
                              child.getLabel().getLocales().stream(),
                              child.getDescription() != null
                                  ? child.getDescription().keySet().stream()
                                  : Stream.of())
                          : Stream.of())
              .collect(Collectors.toList());
      existingLanguages =
          languageService.sortLanguages(LocaleContextHolder.getLocale(), existingLanguages);
    }
    return existingLanguages;
  }

  protected List<Locale> getExistingLanguagesFromService() throws TechnicalException {
    List<Locale> serviceLocales = ((CudamiIdentifiablesClient<I>) service).getLanguages();
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(serviceLocales)) {
      existingLanguages =
          languageService.sortLanguages(LocaleContextHolder.getLocale(), serviceLocales);
    }
    return existingLanguages;
  }

  protected PageResponse<I> search(String searchField, String searchTerm, PageRequest pageRequest)
      throws TechnicalException {
    PageResponse<I> pageResponse;

    if (searchField == null) {
      pageResponse = service.find(pageRequest);
      return pageResponse;
    } else {
      I identifiable;

      switch (searchField) {
        case "label":
          pageResponse = service.find(pageRequest);
          return pageResponse;
        case "uuid":
          identifiable = service.getByUuid(UUID.fromString(searchTerm));
          if (identifiable == null) {
            pageResponse = PageResponse.builder().withContent(new ArrayList<I>()).build();
          } else {
            pageResponse = PageResponse.builder().withContent(identifiable).build();
          }
          pageResponse.setRequest(pageRequest);
          return pageResponse;
        case "identifier":
          Pair<String, String> namespaceAndId = ParameterHelper.extractPairOfStrings(searchTerm);
          identifiable =
              ((CudamiIdentifiablesClient<I>) service)
                  .getByIdentifier(namespaceAndId.getLeft(), namespaceAndId.getRight());
          if (identifiable == null) {
            pageResponse = PageResponse.builder().withContent(new ArrayList<I>()).build();
          } else {
            pageResponse = PageResponse.builder().withContent(identifiable).build();
          }
          pageResponse.setRequest(pageRequest);
          return pageResponse;
        default:
          return null;
      }
    }
  }

  public String doForward(Identifiable identifiable, Model model) throws TechnicalException {
    final String uuid = identifiable.getUuid().toString();
    IdentifiableObjectType identifiableObjectType = identifiable.getIdentifiableObjectType();
    switch (identifiableObjectType) {
      case APPLICATION_FILE_RESOURCE:
      case AUDIO_FILE_RESOURCE:
      case FILE_RESOURCE:
      case IMAGE_FILE_RESOURCE:
      case LINKED_DATA_FILE_RESOURCE:
      case TEXT_FILE_RESOURCE:
      case VIDEO_FILE_RESOURCE:
        return "forward:/fileresources/" + uuid;
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
      case EVENT:
        return "forward:/events/" + uuid;
      case GEO_LOCATION:
      case HUMAN_SETTLEMENT:
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
        HeadwordEntry headwordEntry =
            cudamiClient.forHeadwordEntries().getByUuid(identifiable.getUuid());
        UUID headwordUuid = headwordEntry.getHeadword().getUuid();
        return "redirect:/headwords/" + headwordUuid;
      case ITEM:
        return "forward:/items/" + uuid;
      case MANIFESTATION:
        return "forward:/manifestations/" + uuid;
      case PERSON:
        return "forward:/persons/" + uuid;
      case PROJECT:
        return "forward:/projects/" + uuid;
      case SUBJECT:
        return "forward:/subjects/" + uuid;
      case TOPIC:
        return "forward:/topics/" + uuid;
      case WEBPAGE:
        return "forward:/webpages/" + uuid;
      case WEBSITE:
        return "forward:/websites/" + uuid;
      case WORK:
        return "forward:/works/" + uuid;
      default:
        throw new TechnicalException("Unhandled object type " + identifiableObjectType);
    }
  }

  public String doRedirect(Identifiable identifiable, Model model) throws TechnicalException {
    final String uuid = identifiable.getUuid().toString();
    IdentifiableObjectType identifiableObjectType = identifiable.getIdentifiableObjectType();
    switch (identifiableObjectType) {
      case APPLICATION_FILE_RESOURCE:
      case AUDIO_FILE_RESOURCE:
      case FILE_RESOURCE:
      case IMAGE_FILE_RESOURCE:
      case LINKED_DATA_FILE_RESOURCE:
      case TEXT_FILE_RESOURCE:
      case VIDEO_FILE_RESOURCE:
        return "redirect:/fileresources/" + uuid;
      case ARTICLE:
        return "redirect:/articles/" + uuid;
      case COLLECTION:
        return "redirect:/collections/" + uuid;
      case CORPORATE_BODY:
        return "redirect:/corporatebodies/" + uuid;
      case DIGITAL_OBJECT:
        return "redirect:/digitalobjects/" + uuid;
      case EVENT:
        return "redirect:/events/" + uuid;
      case GEO_LOCATION:
      case HUMAN_SETTLEMENT:
        return "redirect:/geolocations/" + uuid;
      case HEADWORD_ENTRY:
        HeadwordEntry headwordEntry =
            cudamiClient.forHeadwordEntries().getByUuid(identifiable.getUuid());
        UUID headwordUuid = headwordEntry.getHeadword().getUuid();
        return "redirect:/headwords/" + headwordUuid;
      case ITEM:
        return "redirect:/items/" + uuid;
      case MANIFESTATION:
        return "redirect:/manifestations/" + uuid;
      case PERSON:
        return "redirect:/persons/" + uuid;
      case PROJECT:
        return "redirect:/projects/" + uuid;
      case SUBJECT:
        return "redirect:/subjects/" + uuid;
      case TOPIC:
        return "redirect:/topics/" + uuid;
      case WEBPAGE:
        return "redirect:/webpages/" + uuid;
      case WEBSITE:
        return "redirect:/websites/" + uuid;
      case WORK:
        return "redirect:/works/" + uuid;
      default:
        throw new TechnicalException("Unhandled object type " + identifiableObjectType);
    }
  }
}
