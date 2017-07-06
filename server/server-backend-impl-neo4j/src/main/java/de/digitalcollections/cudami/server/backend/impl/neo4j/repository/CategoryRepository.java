package de.digitalcollections.cudami.server.backend.impl.neo4j.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.digitalcollections.cudami.model.api.entity.ContentNode;

@Repository
public interface CategoryRepository extends Neo4jRepository<ContentNode, Long> {

  @Query("MATCH (category:Category) WHERE category.uuid={uuid} RETURN category")
  ContentNode findByUuid(@Param(value = "uuid") String uuid);
}
