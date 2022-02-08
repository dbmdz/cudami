package de.digitalcollections.cudami.client.identifiable.alias;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
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
    super(http, serverUrl, UrlAlias.class, mapper, "/v5/urlaliases");
  }

  public SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        baseEndpoint + "/search", searchPageRequest, LocalizedUrlAliases.class);
  }

  public LocalizedUrlAliases findAllLinks(UUID websiteUuid, String slug) throws TechnicalException {
    if (websiteUuid == null) {
      return (LocalizedUrlAliases)
          doGetRequestForObject(
              String.format(baseEndpoint + "/%s", slug), LocalizedUrlAliases.class);
    }

    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format(baseEndpoint + "/%s/%s", slug, websiteUuid), LocalizedUrlAliases.class);
  }

  public LocalizedUrlAliases findPrimaryLinks(UUID websiteUuid, String slug)
      throws TechnicalException {
    if (websiteUuid == null) {
      return (LocalizedUrlAliases)
          doGetRequestForObject(
              String.format(baseEndpoint + "/primary/%s", slug), LocalizedUrlAliases.class);
    }

    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format(baseEndpoint + "/primary/%s/%s", slug, websiteUuid),
            LocalizedUrlAliases.class);
  }

  public LocalizedUrlAliases findPrimaryLinksForLocale(
      UUID websiteUuid, String slug, Locale pLocale) throws TechnicalException {
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
  }

  public String generateSlug(Locale locale, String label, UUID websiteUuid)
      throws TechnicalException {
    String encodedLabel;
    try {
      encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new TechnicalException("generateSlug", e);
    }

    if (websiteUuid == null) {
      return doGetRequestForString(
          String.format(baseEndpoint + "/slug/%s/%s", locale, encodedLabel));
    }
    return doGetRequestForString(
        String.format(baseEndpoint + "/slug/%s/%s/%s", locale, encodedLabel, websiteUuid));
  }

  public boolean isMainLink(UUID websiteUuid, String slug) throws TechnicalException {
    LocalizedUrlAliases localizedUrlAliases = findPrimaryLinks(websiteUuid, slug);
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
