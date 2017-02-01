package de.digitalcollections.cms.server.backend.impl.neo4j.repository;

import de.digitalcollections.cms.model.api.entity.Category;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface CategoryRepository extends GraphRepository<Category> {

  Category findByUuid(String uuid);
}
