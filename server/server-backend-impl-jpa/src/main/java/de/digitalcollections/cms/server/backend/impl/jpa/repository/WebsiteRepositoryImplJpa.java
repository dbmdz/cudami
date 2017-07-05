package de.digitalcollections.cms.server.backend.impl.jpa.repository;

import de.digitalcollections.cms.server.backend.api.repository.WebsiteRepository;
import de.digitalcollections.cms.server.backend.impl.jpa.entity.WebsiteImplJpa;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Website repository based on JPA.
 */
@Repository
public class WebsiteRepositoryImplJpa extends AbstractPagingAndSortingRepositoryImplJpa<WebsiteImplJpa, Long, WebsiteRepositoryJpa>
        implements WebsiteRepository<WebsiteImplJpa, Long> {

  @Override
  public WebsiteImplJpa create() {
    return new WebsiteImplJpa();
  }

  @Override
  public List<WebsiteImplJpa> findAll(Sort sort) {
    return jpaRepository.findAll(sort);
  }

  @Autowired
  @Override
  void setJpaRepository(WebsiteRepositoryJpa jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

//  @Override
//  public WebsiteImplJpa find(UUID uuid) {
//    Assert.notNull(uuid);
//    QWebsiteImplJpa impl = QWebsiteImplJpa.websiteImplJpa;
//    BooleanExpression booleanExpression = impl.eq(uuid.toString());
//    WebsiteImplJpa result = (WebsiteImplJpa) jpaRepository.findOne(booleanExpression);
//    return result;
//  }
}
