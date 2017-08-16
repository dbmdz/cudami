package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.model.impl.entity.WebsiteImpl;
import de.digitalcollections.cudami.server.backend.api.repository.WebsiteRepository;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl implements WebsiteRepository<Website, UUID> {

  @Autowired
  private Jdbi dbi;

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM websites";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public Website create() {
    return new WebsiteImpl();
  }

  @Override
  public PageResponse<Website> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM websites");

    addPageRequestParams(pageRequest, query);
    List<WebsiteImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(WebsiteImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Website findOne(UUID uuid) {
    List<WebsiteImpl> list = dbi.withHandle(h -> h.createQuery(
            "SELECT * FROM websites WHERE uuid = :uuid")
            .bind("uuid", uuid)
            .mapToBean(WebsiteImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"url"};
  }

  @Override
  public Website save(Website website) {
    WebsiteImpl result = dbi.withHandle(h -> h
            .createQuery("INSERT INTO websites(url, registration_date, uuid) VALUES (:url, :registrationDate, :uuid) RETURNING *")
            .bindBean(website)
            .mapToBean(WebsiteImpl.class)
            .findOnly());
    return result;
  }

  @Override
  public Website update(Website website) {
    WebsiteImpl result = dbi.withHandle(h -> h
            .createQuery("UPDATE websites SET url=:url, registration_date=:registrationDate, uuid=:uuid WHERE uuid=:uuid RETURNING *")
            .bindBean(website)
            .mapToBean(WebsiteImpl.class)
            .findOnly());
    return result;
  }
}
