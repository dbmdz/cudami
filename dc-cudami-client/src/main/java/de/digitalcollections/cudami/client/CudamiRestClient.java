package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CudamiRestClient<T extends UniqueObject> extends BaseRestClient<T> {

  public static final String API_VERSION_PREFIX = "/v6";
  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiRestClient.class);
  //  private final boolean specialHandlingForLabelAndNameFiltering;

  public CudamiRestClient(
      HttpClient http,
      String serverUrl,
      Class<T> targetType,
      ObjectMapper mapper,
      String baseEndpoint) {
    super(http, serverUrl, targetType, mapper, baseEndpoint);
    //    this(http, serverUrl, targetType, mapper, baseEndpoint, true);
  }

  //  public CudamiRestClient(
  //      HttpClient http,
  //      String serverUrl,
  //      Class<T> targetType,
  //      ObjectMapper mapper,
  //      String baseEndpoint,
  //      boolean specialHandlingForLabelAndNameFiltering) {
  //    super(http, serverUrl, targetType, mapper, baseEndpoint);
  //    this.specialHandlingForLabelAndNameFiltering = specialHandlingForLabelAndNameFiltering;
  //  }

  public long count() throws TechnicalException {
    String result = doGetRequestForString(baseEndpoint + "/count");
    return Long.parseLong(result);
  }

  public void deleteByUuid(UUID uuid) throws TechnicalException {
    try {
      doDeleteRequestForString(String.format("%s/%s", baseEndpoint, uuid));
    } catch (ResourceNotFoundException e) {
    }
  }

  public PageResponse<T> find(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public List<T> getAll() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/all");
  }

  public T getByUuid(UUID uuid) throws TechnicalException {
    try {
      return doGetRequestForObject(String.format("%s/%s", baseEndpoint, uuid));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public T save(T object) throws TechnicalException {
    return doPostRequestForObject(baseEndpoint, object);
  }

  public T update(UUID uuid, T object) throws TechnicalException {
    return doPutRequestForObject(String.format("%s/%s", baseEndpoint, uuid), object);
  }

  @Override
  protected String filterCriterionToUrlParam(FilterCriterion filterCriterion) {
    // FIXME: remove special handling of label and name
    // if (specialHandlingForLabelAndNameFiltering) {
    // Matcher labelOrName =
    // Pattern.compile("^(label|name)").matcher(filterCriterion.getExpression());
    // if (labelOrName.find()) {
    // if (filterCriterion.getValue() == null || !(filterCriterion.getValue()
    // instanceof
    // String)) {
    // return "";
    // }
    // String value = (String) filterCriterion.getValue();
    // if (filterCriterion.getOperation() == FilterOperation.EQUALS) {
    // value = String.format("\"%s\"", value);
    // } else if (filterCriterion.getOperation() != FilterOperation.CONTAINS) {
    // throw new UnsupportedOperationException(
    // "`label` and `name` can only be filtered by using CONTAINS (should be
    // preferred)
    // or EQUALS!");
    // }
    // String urlParams = String.format("%s=%s", labelOrName.group(1),
    // URLEncoder.encode(value, StandardCharsets.UTF_8));
    // Matcher matchLanguage =
    // Pattern.compile("\\.([\\w_-]+)$").matcher(filterCriterion.getExpression());
    // if (matchLanguage.find()) {
    // // there is a language defined
    // return urlParams + String.format("&%sLanguage=%s", labelOrName.group(1),
    // URLEncoder.encode(matchLanguage.group(1), StandardCharsets.UTF_8));
    // }
    // return urlParams;
    // }
    // }
    return super.filterCriterionToUrlParam(filterCriterion);
  }
}
