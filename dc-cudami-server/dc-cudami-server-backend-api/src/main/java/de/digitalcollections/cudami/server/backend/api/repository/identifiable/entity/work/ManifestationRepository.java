package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface ManifestationRepository extends EntityRepository<Manifestation> {

  default PageResponse<Manifestation> findManifestationsByWork(Work work, PageRequest pageRequest)
      throws RepositoryException {
    if (work == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findManifestationsByWork(work.getUuid(), pageRequest);
  }

  PageResponse<Manifestation> findManifestationsByWork(UUID workUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<Manifestation> findSubParts(
      Manifestation manifestation, PageRequest pageRequest) throws RepositoryException {
    if (manifestation == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findSubParts(manifestation.getUuid(), pageRequest);
  }

  // TODO: documentation: no parent - child relation; is modelled as
  // List<RelationSpecification<Manifestation>> parents ("partOf");
  // so maybe rename to "findSubParts"? (volumes of a multi-volume manifestation
  // like lexika)
  PageResponse<Manifestation> findSubParts(UUID uuid, PageRequest pageRequest)
      throws RepositoryException;

  default List<Locale> getLanguagesOfManifestationsForWork(Work work) throws RepositoryException {
    if (work == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return getLanguagesOfManifestationsForWork(work.getUuid());
  }

  List<Locale> getLanguagesOfManifestationsForWork(UUID workUuid);
}
