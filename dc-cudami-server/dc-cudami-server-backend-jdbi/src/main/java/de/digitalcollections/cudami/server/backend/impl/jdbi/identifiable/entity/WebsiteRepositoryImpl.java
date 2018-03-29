package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl implements WebsiteRepository<Website> {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WebsiteRepositoryImpl.class);

  @Autowired
  private Jdbi dbi;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private EntityRepository entityRepository;

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
    StringBuilder query = new StringBuilder("SELECT ws.id as id, ws.uuid as uuid, ws.url as url, ws.registration_date as registration_date, ws.rootpages as rootpages"
            + " FROM websites ws INNER JOIN entities e ON ws.uuid=e.uuid INNER JOIN identifiables i ON ws.uuid=i.uuid");
//    StringBuilder query = new StringBuilder("SELECT ws.id as id, ws.uuid as uuid, ws.url as url, ws.registration_date as registration_date FROM websites ws INNER JOIN entities e ON ws.uuid=e.uuid INNER JOIN identifiables i ON ws.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

//    List<Map<String, Object>> list = dbi.withHandle(h -> h.createQuery(query.toString()).mapToMap().list());
    List<WebsiteImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(WebsiteImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
//    PageResponse pageResponse = new PageResponseImpl(null, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Website findOne(UUID uuid) {
//    String query = "SELECT * FROM websites INNER JOIN entities ON websites.uuid=entities.uuid INNER JOIN identifiables ON websites.uuid=identifiables.uuid WHERE websites.uuid = :uuid";
    String query = "SELECT ws.id as id, ws.uuid as uuid, ws.url as url, ws.registration_date as registration_date, ws.rootpages as rootpages"
            + " FROM websites ws INNER JOIN entities e ON ws.uuid=e.uuid INNER JOIN identifiables i ON ws.uuid=i.uuid"
            + " WHERE ws.uuid = :uuid";

    List<WebsiteImpl> list = dbi.withHandle(h -> h.createQuery(query)
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
  public List<Node> getRootNodes(Website website) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Website save(Website website) {
    entityRepository.save(website);

    // TODO use Optional with emptyset, had problem with type...
    //  (Optional.ofNullable(collection).orElse(Collections.emptySet())
    List<UUID> uuidListRootPages = null;
//    List<Webpage> rootPages = website.getRootPages();
//    if (rootPages != null && !rootPages.isEmpty()) {
//      uuidListRootPages = rootPages.stream().map((identifiable) -> identifiable.getUuid()).collect(Collectors.toList());
//    }

    WebsiteImpl result = null;
    try {
      String uuidsRootPages = objectMapper.writeValueAsString(uuidListRootPages);

      result = dbi.withHandle(h -> h
              .createQuery("INSERT INTO websites(url, registration_date, uuid, rootPages) VALUES (:url, :registrationDate, :uuid, :rootPages) RETURNING *")
              //              .bind("rootPages", uuidsRootPages)
              .bindBean(website)
              .mapToBean(WebsiteImpl.class) // FIXME: mapping back from list<uuid> to list<webpage>
              .findOnly());
    } catch (JsonProcessingException ex) {
      LOGGER.error("error saving website", ex);
    }
    return result;
  }

  @Override
  public Website update(Website website) {
    entityRepository.update(website);

    WebsiteImpl result = dbi.withHandle(h -> h
            .createQuery("UPDATE websites SET url=:url, registration_date=:registrationDate, uuid=:uuid WHERE uuid=:uuid RETURNING *")
            .bindBean(website)
            .mapToBean(WebsiteImpl.class)
            .findOnly());
    return result;
  }
}
