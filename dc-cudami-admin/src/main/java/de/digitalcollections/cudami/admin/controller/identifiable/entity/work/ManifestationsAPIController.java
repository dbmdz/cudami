package de.digitalcollections.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
import de.digitalcollections.cudami.admin.model.InvertedRelationSpecification;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTRequest;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiManifestationsClient;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
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
    extends AbstractEntitiesController<Manifestation, CudamiManifestationsClient> {

  public ManifestationsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forManifestations(), client, languageService);
  }

  @SuppressFBWarnings
  @GetMapping("/api/manifestations")
  @ResponseBody
  public BTResponse<Manifestation> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        Manifestation.class,
        offset,
        limit,
        sortProperty,
        sortOrder,
        "label",
        searchTerm,
        dataLanguage);
  }

  /*
   * Used in templates/manifestations/view.html
   */
  @GetMapping("/api/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children")
  @ResponseBody
  public BTResponse<InvertedRelationSpecification<Manifestation>> findChildManifestations(
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
        ((CudamiManifestationsClient) service).findChildren(uuid, btRequest);
    PageResponse<InvertedRelationSpecification<Manifestation>> pageResponseTransformed =
        transformToInvertedRelationSpecification(uuid, pageResponse);
    return new BTResponse<>(pageResponseTransformed);
  }

  /*
   * Used in templates/manifestations/view.html
   */
  @GetMapping("/api/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items")
  @ResponseBody
  public BTResponse<Item> findItems(
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
            Item.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
    PageResponse<Item> pageResponse =
        ((CudamiManifestationsClient) service).findItems(uuid, btRequest);
    return new BTResponse<>(pageResponse);
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
}
