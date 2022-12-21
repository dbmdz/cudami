package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;

public abstract class UniqueObjectRepositoryImpl<U extends UniqueObject> extends JdbiRepositoryImpl
    implements UniqueObjectRepository<U> {

  public UniqueObjectRepositoryImpl(
      Jdbi jdbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      int offsetForAlternativePaging) {
    super(jdbi, tableName, tableAlias, mappingPrefix, offsetForAlternativePaging);
  }

  @Override
  public PageResponse find(PageRequest pageRequest) {
    return null;
  }

  @Override
  public U getByUuid(UUID uuid) {
    return UniqueObjectRepository.super.getByUuid(uuid);
  }

  @Override
  public U getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    return null;
  }

  @Override
  protected String getWhereClause(
      FilterCriterion<?> fc, Map<String, Object> argumentMappings, int criterionCount)
      throws IllegalArgumentException, UnsupportedOperationException {
    Matcher labelOrName = Pattern.compile("^(label|name)").matcher(fc.getExpression());
    if (labelOrName.find()) {
      if (!(fc.getValue() instanceof String)) {
        throw new IllegalArgumentException("Value of label must be a string!");
      }
      String value = (String) fc.getValue();
      switch (fc.getOperation()) {
        case CONTAINS:
          if (argumentMappings.containsKey(SearchTermTemplates.ARRAY_CONTAINS.placeholder)) {
            throw new IllegalArgumentException(
                "Filtering by label and by name are mutually exclusive!");
          }
          argumentMappings.put(
              SearchTermTemplates.ARRAY_CONTAINS.placeholder,
              IdentifiableRepository.splitToArray(value));
          return SearchTermTemplates.ARRAY_CONTAINS.renderTemplate(
              tableAlias, "split_" + labelOrName.group(1));
        case EQUALS:
          if (argumentMappings.containsKey(SearchTermTemplates.JSONB_PATH.placeholder)) {
            throw new IllegalArgumentException(
                "Filtering by label and by name are mutually exclusive!");
          }
          Matcher matchLanguage = Pattern.compile("\\.([\\w_-]+)$").matcher(fc.getExpression());
          String language = matchLanguage.find() ? matchLanguage.group(1) : "**";
          argumentMappings.put(
              SearchTermTemplates.JSONB_PATH.placeholder, escapeTermForJsonpath(value));
          return SearchTermTemplates.JSONB_PATH.renderTemplate(
              tableAlias, labelOrName.group(1), language);
        default:
          throw new UnsupportedOperationException(
              "Filtering by label only supports CONTAINS (to be preferred) or EQUALS operator!");
      }
    }

    return super.getWhereClause(fc, argumentMappings, criterionCount);
  }

  public String[] splitToArray(LocalizedText localizedText) {
    if (localizedText == null) {
      return new String[0];
    }
    List<String> splitLabels =
        localizedText.values().stream()
            .map(text -> IdentifiableRepository.splitToArray(text))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList());
    return splitLabels.toArray(new String[splitLabels.size()]);
  }
}
