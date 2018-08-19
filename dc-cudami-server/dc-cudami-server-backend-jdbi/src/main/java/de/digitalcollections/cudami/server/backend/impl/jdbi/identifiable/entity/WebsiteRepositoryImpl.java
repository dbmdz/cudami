package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.impl.PageResponseImpl;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import de.digitalcollections.model.impl.identifiable.resource.WebpageImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl<W extends WebsiteImpl> extends EntityRepositoryImpl<W> implements WebsiteRepository<W> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteRepositoryImpl.class);

  private final EntityRepository entityRepository;
  private final LocaleRepository localeRepository;

  @Autowired
  public WebsiteRepositoryImpl(
          @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
          @Qualifier("entityRepositoryImpl") EntityRepository entityRepository,
          LocaleRepository localeRepository,
          Jdbi dbi) {
    super(dbi, identifiableRepository);
    this.entityRepository = entityRepository;
    this.localeRepository = localeRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM websites";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public W create() {
    Locale defaultLocale = localeRepository.getDefault();
    W website = (W) new WebsiteImpl();
    website.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    website.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    return website;
  }

  @Override
  public PageResponse<W> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT ws.id as id, ws.uuid as uuid, ws.url as url, ws.registration_date as registration_date, i.label as label, i.description as description")
            .append(" FROM websites ws INNER JOIN entities e ON ws.uuid=e.uuid INNER JOIN identifiables i ON ws.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

    List<WebsiteImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(WebsiteImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public W findOne(UUID uuid) {
    String query = "SELECT ws.uuid as uuid, ws.url as url, ws.registration_date as registration_date, i.label as label, i.description as description"
            + " FROM websites ws INNER JOIN entities e ON ws.uuid=e.uuid INNER JOIN identifiables i ON ws.uuid=i.uuid"
            + " WHERE ws.uuid = :uuid";

    List<WebsiteImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(WebsiteImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    W website = (W) list.get(0);
    website.setRootPages(getRootPages(website));
    return website;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"url"};
  }

  @Override
  public List<Webpage> getRootPages(W website) {
    UUID uuid = website.getUuid();
    return getRootPages(uuid);
  }

  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT ww.webpage_uuid as uuid, i.label as label"
            + " FROM websites ws INNER JOIN website_webpage ww ON ws.uuid=ww.website_uuid INNER JOIN identifiables i ON ww.webpage_uuid=i.uuid"
            + " WHERE ws.uuid = :uuid";

    List<WebpageImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(WebpageImpl.class)
            .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(WebpageImpl.class::cast).collect(Collectors.toList());
  }

  @Override
  public W save(W website) {
    entityRepository.save(website);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO websites(url, registration_date, uuid) VALUES (:url, :registrationDate, :uuid)")
            .bindBean(website)
            .execute());
    return findOne(website.getUuid());
  }

  @Override
  public W update(W website) {
    entityRepository.update(website);
    // do not update/left out from statement: created, uuid
    dbi.withHandle(h -> h.createUpdate("UPDATE websites SET url=:url, registration_date=:registrationDate WHERE uuid=:uuid")
            .bindBean(website)
            .execute());
    return findOne(website.getUuid());
  }
}
