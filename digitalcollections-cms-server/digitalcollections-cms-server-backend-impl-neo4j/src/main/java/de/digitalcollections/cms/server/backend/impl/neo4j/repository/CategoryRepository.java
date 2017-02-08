package de.digitalcollections.cms.server.backend.impl.neo4j.repository;

import de.digitalcollections.cms.model.api.entity.Category;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends Neo4jRepository<Category, Long> {

  @Query("MATCH (category:Category) WHERE category.uuid={uuid} RETURN category")
  Category findByUuid(@Param(value = "uuid") String uuid);
}
