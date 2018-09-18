package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.parts.Translation;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl<W extends Webpage> extends IdentifiableRepositoryImpl<W> implements WebpageRepository<W> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  private final IdentifiableRepository identifiableRepository;
  private final LocaleRepository localeRepository;

  @Autowired
  public WebpageRepositoryImpl(
          @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
          @Qualifier("resourceRepositoryImpl") ResourceRepository resourceRepository,
          LocaleRepository localeRepository,
          Jdbi dbi) {
    this.dbi = dbi;
    this.identifiableRepository = identifiableRepository;
    this.localeRepository = localeRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM webpages";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public W create() {
    Locale defaultLocale = localeRepository.getDefault();
    W webpage = (W) new WebpageImpl();
    webpage.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    webpage.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    webpage.setText(new LocalizedStructuredContentImpl(defaultLocale));
    return webpage;
  }

  @Override
  public PageResponse<W> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder()
            .append("SELECT wp.uuid as uuid, wp.text as text, i.label as label, i.description as description")
            .append(" FROM webpages wp INNER JOIN resources r ON wp.uuid=r.uuid INNER JOIN identifiables i ON wp.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

//    List<Map<String, Object>> list = dbi.withHandle(h -> h.createQuery(query.toString()).mapToMap().list());
    List<WebpageImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(WebpageImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
//    PageResponse pageResponse = new PageResponseImpl(null, pageRequest, total);
    return pageResponse;
  }

  @Override
  public W findOne(UUID uuid) {
    String query = "SELECT wp.uuid as uuid, wp.text as text, i.label as label, i.description as description"
            + " FROM webpages wp INNER JOIN resources r ON wp.uuid=r.uuid INNER JOIN identifiables i ON wp.uuid=i.uuid"
            + " WHERE wp.uuid = :uuid";

    List<WebpageImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(WebpageImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    W webpage = (W) list.get(0);
    webpage.setChildren(getChildren(webpage));
    return webpage;
  }

  @Override
  public W findOne(UUID uuid, Locale locale) {
    W webpage = findOne(uuid);
    Set<Translation> translations = webpage.getLabel().getTranslations();

    if (locale == null) {
      // just return first existing locale
      Optional<Translation> translation = translations.stream().findFirst();
      locale = translation.map(Translation::getLocale).orElse(null);
    }
    final Locale fLocale = locale;
    if (fLocale == null) {
      // a webpage/identifiable without label does not make sense...
      return null;
    }

    // if requested locale does not exist, return null
    boolean requestedTranslationExists = translations.stream().anyMatch(translation -> translation.getLocale().equals(fLocale));
    if (!requestedTranslationExists) {
      return null;
    }

    // TODO maybe a better solution to just get locale specific fields directly from database instead of removing it here?
    // iterate over all localized fields and remove all texts that are not matching the requested locale:
    webpage.getLabel().getTranslations().removeIf(translation -> !translation.getLocale().equals(fLocale));
    webpage.getDescription().getLocalizedStructuredContent().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    webpage.getText().getLocalizedStructuredContent().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(fLocale));
    return webpage;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public List<W> getChildren(W webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public List<W> getChildren(UUID uuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT ww.child_webpage_uuid as uuid, i.label as label"
            + " FROM webpages wp INNER JOIN webpage_webpage ww ON wp.uuid=ww.parent_webpage_uuid INNER JOIN identifiables i ON ww.child_webpage_uuid=i.uuid"
            + " WHERE wp.uuid = :uuid"
            + " ORDER BY ww.sortIndex ASC";

    List<WebpageImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(WebpageImpl.class)
            .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(s -> (W) s).collect(Collectors.toList());
  }

  @Override
  public W save(W webpage) {
    identifiableRepository.save(webpage);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO webpages(uuid, text) VALUES (:uuid, :text::JSONB)")
            .bindBean(webpage)
            .execute());

    return findOne(webpage.getUuid());
  }

  @Override
  public W saveWithParentWebsite(W webpage, UUID parentWebsiteUuid) {
    identifiableRepository.save(webpage);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO webpages(uuid, text) VALUES (:uuid, :text::JSONB)")
            .bindBean(webpage)
            .execute());

    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "website_webpage", "parent_website_uuid", parentWebsiteUuid);
    dbi.withHandle(h -> h.createUpdate(
            "INSERT INTO website_webpage(website_uuid, webpage_uuid, sortIndex)"
            + " VALUES (:parent_website_uuid, :uuid, :sortIndex)")
            .bind("parent_website_uuid", parentWebsiteUuid)
            .bind("sortIndex", sortIndex)
            .bindBean(webpage)
            .execute());

    return findOne(webpage.getUuid());
  }

  @Override
  public W saveWithParentWebpage(W webpage, UUID parentWebpageUuid) {
    identifiableRepository.save(webpage);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO webpages(uuid, text) VALUES (:uuid, :text::JSONB)")
            .bindBean(webpage)
            .execute());

    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "webpage_webpage", "parent_webpage_uuid", parentWebpageUuid);
    dbi.withHandle(h -> h.createUpdate(
            "INSERT INTO webpage_webpage(parent_webpage_uuid, child_webpage_uuid, sortIndex)"
            + " VALUES (:parent_webpage_uuid, :uuid, :sortIndex)")
            .bind("parent_webpage_uuid", parentWebpageUuid)
            .bind("sortIndex", sortIndex)
            .bindBean(webpage)
            .execute());

    return findOne(webpage.getUuid());
  }

  @Override
  public W update(W webpage) {
    identifiableRepository.update(webpage);
    dbi.withHandle(h -> h.createUpdate("UPDATE webpages SET text=:text::JSONB WHERE uuid=:uuid")
            .bindBean(webpage)
            .execute());
    return findOne(webpage.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(W webpage) {
    return getIdentifiables(webpage.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(UUID uuid) {
    // minimal data required for creating text links in a list
    String query = "SELECT i.uuid as uuid, i.label as label"
            + " FROM identifiables i INNER JOIN webpage_identifiables wi ON wi.identifiable_uuid=i.uuid"
            + " WHERE wi.webpage_uuid = :uuid"
            + " ORDER BY wi.sortIndex ASC";

    List<IdentifiableImpl> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(IdentifiableImpl.class)
            .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(Identifiable.class::cast).collect(Collectors.toList());
  }
}
