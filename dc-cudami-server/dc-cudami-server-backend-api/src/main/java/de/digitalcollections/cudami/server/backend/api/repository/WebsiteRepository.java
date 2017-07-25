package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.io.Serializable;
import java.util.List;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> entity instance
 * @param <ID> unique id
 */
public interface WebsiteRepository<W extends Website, ID extends Serializable> {

  W create();

  PageResponse<W> find(PageRequest pageRequest);

  List<W> findAll();

  W findOne(Long id);
//  T find(UUID uuid);

  W save(W website);
}
