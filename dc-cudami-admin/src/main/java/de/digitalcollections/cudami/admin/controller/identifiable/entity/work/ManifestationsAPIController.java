package de.digitalcollections.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.model.InvertedRelationSpecification;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiManifestationsClient;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Manifestations" endpoints (API). */
@RestController
public class ManifestationsAPIController
    extends AbstractIdentifiablesController<Manifestation, CudamiManifestationsClient> {

  public ManifestationsAPIController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forManifestations(), languageSortingHelper, client.forLocales());
  }

  @SuppressFBWarnings
  @GetMapping("/api/manifestations")
  @ResponseBody
  public BTResponse<Manifestation> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "url") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    PageResponse<Manifestation> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }

  /*
  Used in templates/manifestations/view.html
  */
  @GetMapping("/api/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items")
  @ResponseBody
  public BTResponse<Item> findItems(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    // FIXME: sorting crashes (maybe because of "label_de.asc.ignoreCase" / locale problem
    PageRequest pageRequest =
        createPageRequest(null, null, dataLanguage, localeService, offset, limit, searchTerm);
    PageResponse<Item> pageResponse = service.findItems(uuid, pageRequest);
    return new BTResponse<>(pageResponse);
  }

  /*
  Used in templates/manifestations/view.html
  */
  @GetMapping("/api/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children")
  @ResponseBody
  public BTResponse<InvertedRelationSpecification<Manifestation>> findChildManifestations(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    PageRequest pageRequest =
        createPageRequest(sort, order, dataLanguage, localeService, offset, limit, searchTerm);
    PageResponse<InvertedRelationSpecification<Manifestation>> pageResponse =
        transformToInvertedRelationSpecification(uuid, service.findChildren(uuid, pageRequest));
    return new BTResponse<>(pageResponse);
  }

  protected PageResponse<InvertedRelationSpecification<Manifestation>>
      transformToInvertedRelationSpecification(
          UUID parentManifestationUuid, PageResponse<Manifestation> children) {
    PageResponse<InvertedRelationSpecification<Manifestation>> ret = new PageResponse<>();
    ret.setRequest(children.getRequest());
    ret.setExecutedSearchTerm(children.getExecutedSearchTerm());
    ret.setTotalElements(children.getTotalElements());
    ret.setContent(
        children.getContent().stream()
            .map(m -> toInvertedRelationSpecification(parentManifestationUuid, m))
            .collect(Collectors.toList()));
    return ret;
  }

  private InvertedRelationSpecification<Manifestation> toInvertedRelationSpecification(
      UUID parentManifstationUuid, Manifestation manifestation) {
    InvertedRelationSpecification<Manifestation> ret = new InvertedRelationSpecification<>();
    ret.setObject(manifestation);

    RelationSpecification<Manifestation> parentRelationSpecification =
        manifestation.getParents().stream()
            .filter(r -> r.getSubject().getUuid().equals(parentManifstationUuid))
            .findFirst()
            .orElse(null);

    if (parentRelationSpecification != null) {
      ret.setTitle(parentRelationSpecification.getTitle());
      ret.setSortKey(parentRelationSpecification.getSortKey());
    }

    return ret;
  }
}
