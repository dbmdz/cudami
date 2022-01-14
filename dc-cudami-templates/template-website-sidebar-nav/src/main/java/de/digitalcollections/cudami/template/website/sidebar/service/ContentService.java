package de.digitalcollections.cudami.template.website.sidebar.service;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.cudami.client.identifiable.web.CudamiWebpagesClient;
import de.digitalcollections.cudami.template.website.sidebar.config.CudamiConfig;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final CudamiConfig cudamiConfig;
  private final CudamiWebpagesClient cudamiWebpagesClient;
  private final CudamiWebsitesClient cudamiWebsitesClient;

  public ContentService(CudamiConfig cudamiConfig, CudamiClient cudamiClient) {
    this.cudamiConfig = cudamiConfig;
    this.cudamiWebpagesClient = cudamiClient.forWebpages();
    this.cudamiWebsitesClient = cudamiClient.forWebsites();
  }

  private Locale getLocale(Webpage webpage) {
    if (webpage == null) {
      return null;
    }
    return webpage.getLabel().getLocales().iterator().next();
  }

  public List<Webpage> getSitemap() {
    try {
      // get website
      Website website = getWebsite();

      // get root webpages
      PageRequest pageRequest = PageRequest.defaultBuilder().pageSize(100).build();
      PageResponse<Webpage> rootPagesResponse =
          cudamiWebsitesClient.getRootPages(website.getUuid(), pageRequest);
      List<Webpage> allRootWebpages = rootPagesResponse.getContent();

      List<Webpage> activeRootWebpages =
          allRootWebpages.stream()
              .filter(
                  f -> {
                    LocalDate now = LocalDate.now();
                    return (f.getPublicationStart() != null
                            && f.getPublicationStart().compareTo(now) <= 0)
                        && (f.getPublicationEnd() == null
                            || f.getPublicationEnd().compareTo(now) > 0);
                  })
              .collect(Collectors.toList());

      // fill active children tree of each active root webpage
      for (Webpage activeRootWebpage : activeRootWebpages) {
        List<Webpage> activeChildrenTree =
            cudamiWebpagesClient.getActiveChildrenTree(activeRootWebpage.getUuid());
        activeRootWebpage.setChildren(activeChildrenTree);
      }
      return activeRootWebpages;
    } catch (HttpException ex) {
      log.warn("Could not fetch root webpages due to exc={}", ex);
      return null;
    }
  }

  public Pair<Webpage, Locale> getWebpage(UUID uuid) {
    Webpage webpage;
    Locale webpageLocale;
    try {
      Locale locale = Locale.forLanguageTag(LocaleContextHolder.getLocale().getLanguage());
      webpage = cudamiWebpagesClient.findActiveOne(uuid, locale);
    } catch (HttpException ex) {
      log.warn("Could not fetch webpage with uuid={} due to exc={}", uuid.toString(), ex);
      webpage = null;
    }
    webpageLocale = getLocale(webpage);
    return Pair.of(webpage, webpageLocale);
  }

  public Website getWebsite() {
    UUID websiteUuid = cudamiConfig.getWebsite();
    try {
      return cudamiWebsitesClient.findOne(websiteUuid);
    } catch (HttpException ex) {
      log.error("Website with UUID {} can not be loaded.", websiteUuid);
      return null;
    }
  }
}
