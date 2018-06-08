package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.identifiable.parts.Translation;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
import de.digitalcollections.cudami.server.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.prosemirror.model.api.Document;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ParagraphImpl;

import java.util.*;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl implements WebpageRepository<Webpage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  @Autowired
  private Jdbi dbi;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  LocaleRepository localeRepository;

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM webpages";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public Webpage create() {
    Locale defaultLocale = localeRepository.getDefault();
    WebpageImpl webpage = new WebpageImpl();
    webpage.setLabel(new TextImpl(defaultLocale, ""));
    webpage.setDescription(createEmptyMLD(defaultLocale));
    webpage.setText(createEmptyMLD(defaultLocale));
    return webpage;
  }

  private MultilanguageDocument createEmptyMLD(Locale defaultLocale) {
    MultilanguageDocument emptyMLD = new MultilanguageDocumentImpl();
    Document document = new DocumentImpl();
    document.addContentBlock(new ParagraphImpl());
    emptyMLD.addDocument(defaultLocale, document);
    return emptyMLD;
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT wp.uuid as uuid, wp.text as text, i.label as label, i.description as description")
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
  public Webpage findOne(UUID uuid) {
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
    return list.get(0);
  }

  @Override
  public Webpage findOne(UUID uuid, Locale locale) {
    Webpage webpage = findOne(uuid);
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
    webpage.getDescription().getDocuments().entrySet().removeIf(entry -> !entry.getKey().equals(fLocale));
    webpage.getText().getDocuments().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(fLocale));
    return webpage;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public Webpage save(Webpage webpage) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Webpage save(Webpage webpage, UUID websiteUuid) {
    resourceRepository.save(webpage);

    dbi.withHandle(h -> h.createUpdate("INSERT INTO webpages(uuid, text) VALUES (:uuid, :text::JSONB)")
            .bindBean(webpage)
            .execute());

    dbi.withHandle(h -> {
      return h.createUpdate("INSERT INTO website_webpage(website_uuid, webpage_uuid) VALUES (:website_uuid, :uuid)")
              .bind("website_uuid", websiteUuid)
              .bindBean(webpage)
              .execute();
    });

    return findOne(webpage.getUuid());
  }

  @Override
  public Webpage update(Webpage webpage) {
    resourceRepository.update(webpage);
    dbi.withHandle(h -> h.createUpdate("UPDATE webpages SET text=:text::JSONB WHERE uuid=:uuid")
            .bindBean(webpage)
            .execute());
    return findOne(webpage.getUuid());
  }
}
