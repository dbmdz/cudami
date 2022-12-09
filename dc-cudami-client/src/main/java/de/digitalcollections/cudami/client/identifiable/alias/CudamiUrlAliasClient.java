package de.digitalcollections.cudami.client.identifiable.alias;

import static de.digitalcollections.cudami.client.CudamiRestClient.API_VERSION_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class CudamiUrlAliasClient extends BaseRestClient<UrlAlias> {

  public CudamiUrlAliasClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, UrlAlias.class, mapper, API_VERSION_PREFIX + "/urlaliases");
  }

  public PageResponse<LocalizedUrlAliases> find(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(baseEndpoint, pageRequest, LocalizedUrlAliases.class);
  }

  public String generateSlug(Locale locale, String label, UUID websiteUuid)
      throws TechnicalException {
    String encodedLabel;
    try {
      encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new TechnicalException("generateSlug", e);
    }

    try {
      if (websiteUuid == null) {
        return doGetRequestForString(
            String.format(baseEndpoint + "/slug/%s/%s", locale, encodedLabel));
      }
      return doGetRequestForString(
          String.format(baseEndpoint + "/slug/%s/%s/%s", locale, encodedLabel, websiteUuid));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public LocalizedUrlAliases getPrimaryLinks(UUID websiteUuid, String slug)
      throws TechnicalException {
    try {
      if (websiteUuid == null) {
        return (LocalizedUrlAliases)
            doGetRequestForObject(
                String.format(baseEndpoint + "/primary/%s", slug), LocalizedUrlAliases.class);
      }

      return (LocalizedUrlAliases)
          doGetRequestForObject(
              String.format(baseEndpoint + "/primary/%s/%s", slug, websiteUuid),
              LocalizedUrlAliases.class);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public LocalizedUrlAliases getPrimaryLinksForLocale(UUID websiteUuid, String slug, Locale pLocale)
      throws TechnicalException {
    try {
      if (websiteUuid == null) {
        return (LocalizedUrlAliases)
            doGetRequestForObject(
                String.format(baseEndpoint + "/primary/%s?pLocale=%s", slug, pLocale),
                LocalizedUrlAliases.class);
      }

      return (LocalizedUrlAliases)
          doGetRequestForObject(
              String.format(baseEndpoint + "/primary/%s/%s?pLocale=%s", slug, websiteUuid, pLocale),
              LocalizedUrlAliases.class);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public boolean isPrimary(UUID websiteUuid, String slug) throws TechnicalException {
    LocalizedUrlAliases localizedUrlAliases = getPrimaryLinks(websiteUuid, slug);
    if (localizedUrlAliases == null || localizedUrlAliases.isEmpty()) {
      return false;
    }

    for (UrlAlias urlAlias :
        localizedUrlAliases.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
      if (urlAlias.getSlug().equals(slug)) {
        return true;
      }
    }

    return false;
  }
}
