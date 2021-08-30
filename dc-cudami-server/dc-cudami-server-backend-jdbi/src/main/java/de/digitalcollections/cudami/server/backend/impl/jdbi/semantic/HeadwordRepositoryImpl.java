package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class HeadwordRepositoryImpl extends JdbiRepositoryImpl implements HeadwordRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordRepositoryImpl.class);
  public static final String MAPPING_PREFIX = "hword";

  public static final String SQL_REDUCED_FIELDS_HW =
      " hw.uuid hword_uuid, hw.label hword_label, hw.language hword_locale,"
          + " hw.created hword_created, hw.last_modified hword_lastModified";

  public static final String SQL_FULL_FIELDS_HW = SQL_REDUCED_FIELDS_HW;

  public static final String TABLE_ALIAS = "hw";
  public static final String TABLE_NAME = "headwords";

  @Autowired
  public HeadwordRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
  }

  @Override
  public void delete(String label, Locale locale) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM " + tableName + " WHERE label = :label AND language = :language")
                .bind("label", label)
                .bind("language", locale)
                .execute());
  }

  @Override
  public void delete(UUID uuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .execute());
  }

  @Override
  public PageResponse<Headword> find(PageRequest pageRequest) {
    String commonSql = " FROM " + tableName + " AS " + tableAlias;

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    String orderBy = getOrderBy(pageRequest.getSorting());
    if (StringUtils.hasText(orderBy)) {
      orderBy = " ORDER BY " + orderBy;
    }
    List<Headword> result = retrieveList(SQL_REDUCED_FIELDS_HW, innerQuery, orderBy);

    StringBuilder sqlCount = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, sqlCount);
    long total = retrieveCount(sqlCount);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public List<Headword> findAll() {
    return retrieveList(SQL_REDUCED_FIELDS_HW, null, null);
  }

  @Override
  public List<Headword> findByLabel(String label) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Headword findOne(UUID uuid) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Headword findOneByLabelAndLocale(String label, Locale locale) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected String getColumnName(String modelProperty) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  public long retrieveCount(StringBuilder sqlCount) {
    long total =
        dbi.withHandle(h -> h.createQuery(sqlCount.toString()).mapTo(Long.class).findOne().get());
    return total;
  }

  public List<Headword> retrieveList(String fieldsSql, StringBuilder innerQuery, String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null ? " " + orderBy : "");

    List<Headword> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle.createQuery(sql).mapToBean(Headword.class).collect(Collectors.toList());
            });
    return result;
  }

  @Override
  public Headword save(Headword headword) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }
}
