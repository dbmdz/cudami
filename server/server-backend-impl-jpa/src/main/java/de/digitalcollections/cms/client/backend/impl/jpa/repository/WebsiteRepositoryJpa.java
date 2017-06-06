package de.digitalcollections.cms.client.backend.impl.jpa.repository;

import de.digitalcollections.cms.client.backend.impl.jpa.entity.WebsiteImplJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * To execute Querydsl predicates we simply let our repository extend QueryDslPredicateExecutor.
 */
public interface WebsiteRepositoryJpa extends JpaRepository<WebsiteImplJpa, Long>, QueryDslPredicateExecutor {

}
