package de.digitalcollections.cudami.client.identifiable.alias;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
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

public class CudamiUrlAliasClient extends CudamiBaseClient<UrlAlias> {

  public CudamiUrlAliasClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, UrlAlias.class, mapper);
  }

  public LocalizedUrlAliases findPrimaryLinks(UUID websiteUuid, String slug) throws HttpException {
    if (websiteUuid == null) {
      return (LocalizedUrlAliases)
          doGetRequestForObject(
              String.format("/v5/urlaliases/primary/%s", slug), LocalizedUrlAliases.class);
    }

    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format("/v5/urlaliases/primary/%s/%s", slug, websiteUuid),
            LocalizedUrlAliases.class);
  }

  public LocalizedUrlAliases findPrimaryLinksForLocale(
      UUID websiteUuid, String slug, Locale pLocale) throws HttpException {
    if (websiteUuid == null) {
      return (LocalizedUrlAliases)
          doGetRequestForObject(
              String.format("/v5/urlaliases/primary/%s?pLocale=%s", slug, pLocale),
              LocalizedUrlAliases.class);
    }

    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format("/v5/urlaliases/primary/%s/%s?pLocale=%s", slug, websiteUuid, pLocale),
            LocalizedUrlAliases.class);
  }

  public LocalizedUrlAliases findAllLinks(UUID websiteUuid, String slug) throws HttpException {
    if (websiteUuid == null) {
      return (LocalizedUrlAliases)
          doGetRequestForObject(
              String.format("/v5/urlaliases/%s", slug), LocalizedUrlAliases.class);
    }

    return (LocalizedUrlAliases)
        doGetRequestForObject(
            String.format("/v5/urlaliases/%s/%s", slug, websiteUuid), LocalizedUrlAliases.class);
  }

  public UrlAlias save(UrlAlias urlAlias) throws HttpException {
    return doPostRequestForObject("/v5/urlaliases", urlAlias);
  }

  public UrlAlias update(UUID uuid, UrlAlias urlAlias) throws HttpException {
    return doPutRequestForObject(String.format("/v5/urlaliases/%s", uuid), urlAlias);
  }

  public UrlAlias findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/urlaliases/%s", uuid));
  }

  public boolean delete(UUID uuid) throws HttpException {
    return Boolean.parseBoolean(doDeleteRequestForString(String.format("/v5/urlaliases/%s", uuid)));
  }

  public SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        "/v5/urlaliases/search", searchPageRequest, LocalizedUrlAliases.class);
  }

  public boolean isPrimary(UUID websiteUuid, String slug) throws HttpException {
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

  public String generateSlug(Locale locale, String label, UUID websiteUuid) throws HttpException {
    String encodedLabel;
    try {
      encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new HttpException("generateSlug", e);
    }

    if (websiteUuid == null) {
      return doGetRequestForString(
          String.format("/v5/urlaliases/slug/%s/%s", locale, encodedLabel));
    }
    return doGetRequestForString(
        String.format("/v5/urlaliases/slug/%s/%s/%s", locale, encodedLabel, websiteUuid));
  }
}
