package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.NullHandling;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;

public class CudamiIdentifiablesClient<I extends Identifiable> extends CudamiRestClient<I> {

  public CudamiIdentifiablesClient(
      HttpClient http,
      String serverUrl,
      Class<I> identifiableClass,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, identifiableClass, mapper, baseEndpoint);
  }

  public CudamiIdentifiablesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(
        http,
        serverUrl,
        (Class<I>) Identifiable.class,
        mapper,
        API_VERSION_PREFIX + "/identifiables");
  }

  @Override
  protected String filterCriterionToUrlParam(FilterCriterion filterCriterion) {
    Matcher labelOrName = Pattern.compile("^(label|name)").matcher(filterCriterion.getExpression());
    if (labelOrName.find()) {
      if (filterCriterion.getValue() == null || !(filterCriterion.getValue() instanceof String)) {
        return "";
      }
      String value = (String) filterCriterion.getValue();
      if (filterCriterion.getOperation() == FilterOperation.EQUALS) {
        value = String.format("\"%s\"", value);
      } else if (filterCriterion.getOperation() != FilterOperation.CONTAINS) {
        throw new UnsupportedOperationException(
            "`label` and `name` can only be filtered by using CONTAINS (should be preferred) or EQUALS!");
      }
      String urlParams =
          String.format(
              "%s=%s", labelOrName.group(1), URLEncoder.encode(value, StandardCharsets.UTF_8));
      Matcher matchLanguage =
          Pattern.compile("\\.([\\w_-]+)$").matcher(filterCriterion.getExpression());
      if (matchLanguage.find()) {
        // there is a language defined
        return urlParams
            + String.format(
                "&%sLanguage=%s",
                labelOrName.group(1),
                URLEncoder.encode(matchLanguage.group(1), StandardCharsets.UTF_8));
      }
      return urlParams;
    }

    return super.filterCriterionToUrlParam(filterCriterion);
  }

  public List<I> find(String searchTerm, int maxResults) throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, 0, maxResults, null);
    PageResponse<I> response = find(pageRequest);
    return response.getContent();
  }

  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "?language=%s&initial=%s", language, initial), pageRequest);
  }

  public PageResponse<I> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws TechnicalException {
    Order order =
        new Order(
            Direction.fromString(sortDirection), sortField, NullHandling.valueOf(nullHandling));
    Sorting sorting = new Sorting(order);
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sorting);
    return findByLanguageAndInitial(pageRequest, language, initial);
  }

  /**
   * Retrieves an Identifiable by its namespace and id and appends additional parameters to the
   * request
   *
   * @param namespace the namespace. Must be plain text, not encoded in any way
   * @param id the id. Must be in plain text, not encoded in any way
   * @param additionalParameters a map&lt;String,String&gt; of additional parameters
   * @return the Identifiable or null
   * @throws TechnicalException in case of an error
   */
  public I getByIdentifier(String namespace, String id, Map<String, String> additionalParameters)
      throws TechnicalException {
    String namespaceAndId = namespace + ":" + id;

    String encodedNamespaceAndId =
        Base64.encodeBase64URLSafeString(namespaceAndId.getBytes(StandardCharsets.UTF_8));

    String expandedAdditionalParameters = "";
    if (additionalParameters != null && !additionalParameters.isEmpty()) {
      expandedAdditionalParameters =
          "?"
              + additionalParameters.entrySet().stream()
                  .map(e -> e.getKey() + "=" + e.getValue())
                  .collect(Collectors.joining("&"));
    }

    try {
      return doGetRequestForObject(
          String.format(
              baseEndpoint + "/identifier/%s%s",
              encodedNamespaceAndId,
              expandedAdditionalParameters));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  /**
   * Retrieves an Identifiable by its namespace and id
   *
   * @param namespace the namespace. Must be plain text, not encoded in any way
   * @param id the id. Must be in plain text, not encoded in any way
   * @return the Identifiable or null
   * @throws TechnicalException in case of an error
   */
  public I getByIdentifier(String namespace, String id) throws TechnicalException {
    return getByIdentifier(namespace, id, null);
  }

  public I getByUuidAndLocale(UUID uuid, Locale locale) throws TechnicalException {
    return getByUuidAndLocale(uuid, locale.toString());
  }

  public I getByUuidAndLocale(UUID uuid, String locale) throws TechnicalException {
    try {
      return doGetRequestForObject(String.format(baseEndpoint + "/%s?locale=%s", uuid, locale));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return this.doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }

  public LocalizedUrlAliases getLocalizedUrlAliases(UUID uuid) throws TechnicalException {
    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format(baseEndpoint + "/%s/localizedUrlAliases", uuid),
            LocalizedUrlAliases.class);
  }
}
