package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.identifiable.entity.NamedEntity;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class UniqueObjectServiceImpl<
        U extends UniqueObject, R extends UniqueObjectRepository<U>>
    implements UniqueObjectService<U> {

  protected R repository;

  protected UniqueObjectServiceImpl(R repository) {
    this.repository = repository;
  }

  /**
   * Special logic to filter by label, optionally paying attention to the language. The passed
   * {@code PageResponse} could be modified.
   *
   * @param pageResponse the response from the repo, must always contain the request too (if
   *     everything goes right)
   */
  protected void filterBySplitField(
      PageResponse<U> pageResponse,
      FilterCriterion<String> filter,
      Function<U, Optional<LocalizedText>> retrieveField) {
    if (!pageResponse.hasContent()) {
      return;
    }
    // we must differentiate several cases
    if (filter.getOperation() == FilterOperation.EQUALS) {
      // everything has been done by repo already
      return;
    }

    // for CONTAINS the language, if any, has not been taken into account yet
    Matcher matchLanguage = Pattern.compile("\\.([\\w_-]+)$").matcher(filter.getExpression());
    if (matchLanguage.find()) {
      // there is a language...
      Locale language = Locale.forLanguageTag(matchLanguage.group(1));
      List<String> searchTerms =
          Arrays.asList(IdentifiableRepository.splitToArray((String) filter.getValue()));
      List<U> filteredContent =
          pageResponse.getContent().parallelStream()
              .filter(
                  uniqueObject -> {
                    String text =
                        retrieveField.apply(uniqueObject).orElse(new LocalizedText()).get(language);
                    if (text == null) {
                      return false;
                    }
                    List<String> splitText =
                        Arrays.asList(IdentifiableRepository.splitToArray(text));
                    return splitText.containsAll(searchTerms);
                  })
              .collect(Collectors.toList());
      // fix total elements count roughly
      pageResponse.setTotalElements(
          pageResponse.getTotalElements()
              - (pageResponse.getContent().size() - filteredContent.size()));
      pageResponse.setContent(filteredContent);
    }
  }

  @Override
  public PageResponse<U> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    // filter by label or name (NamedEntity) is quite special due to optimization
    FilterCriterion<String> labelFilter = null;
    FilterCriterion<String> nameFilter = null;
    if (pageRequest.hasFiltering()) {
      labelFilter =
          pageRequest.getFiltering().getFilterCriteria().stream()
              .filter(fc -> fc.getExpression().startsWith("label"))
              .findAny()
              .orElse(null);
      nameFilter =
          pageRequest.getFiltering().getFilterCriteria().stream()
              .filter(fc -> fc.getExpression().startsWith("name"))
              .findAny()
              .orElse(null);
    }
    PageResponse<U> response = repository.find(pageRequest);
    if (labelFilter == null && nameFilter == null) {
      // nothing special here, go on
      return response;
    }
    // filter by label or name specials go here, it is an either-or though
    if (labelFilter != null) {
      filterBySplitField(response, labelFilter, extractLabelFunction());
    } else {
      filterBySplitField(
          response,
          nameFilter,
          i -> i instanceof NamedEntity ne ? Optional.ofNullable(ne.getName()) : Optional.empty());
    }
    // TODO: what happens if all entries have been removed by the filter?
    return response;
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    // business logic: default sorting if no other sorting given: lastModified descending, uuid
    // ascending
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(new Order(Direction.DESC, "lastModified"), new Order("uuid"));
      pageRequest.setSorting(sorting);
    }
  }

  protected abstract Function<U, Optional<LocalizedText>> extractLabelFunction();
}
