package de.digitalcollections.cudami.frontend.website.service;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.cudami.client.identifiable.web.CudamiWebpagesClient;
import de.digitalcollections.cudami.frontend.website.config.CudamiConfig;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentService.class);

  private final CudamiConfig cudamiConfig;
  private final CudamiWebpagesClient cudamiWebpagesClient;
  private final CudamiWebsitesClient cudamiWebsitesClient;

  public ContentService(CudamiConfig cudamiConfig, CudamiClient cudamiClient) {
    this.cudamiConfig = cudamiConfig;
    this.cudamiWebpagesClient = cudamiClient.forWebpages();
    this.cudamiWebsitesClient = cudamiClient.forWebsites();
  }

  public List<Webpage> getContentPages() {
    try {
      // get website
      Website website = getWebsite();

      List<Webpage> allContentWebpages;

      if (cudamiConfig.getWebpage("content") == null) {
        // get root webpages
        PageRequest pageRequest = PageRequest.defaultBuilder().pageSize(100).build();
        PageResponse<Webpage> rootPagesResponse =
            cudamiWebsitesClient.getRootPages(website.getUuid(), pageRequest);
        allContentWebpages = rootPagesResponse.getContent();
      } else {
        Pair<Webpage, Locale> webpagePair = getWebpage(cudamiConfig.getWebpage("content"));
        Webpage webpage = webpagePair.getLeft();
        allContentWebpages = webpage.getChildren();
      }

      List<Webpage> activeContentWebpages =
          allContentWebpages.stream()
              .filter(
                  f -> {
                    return isActive(f);
                  })
              .collect(Collectors.toList());

      // fill active children tree of each active root webpage
      for (Webpage activeContentWebpage : activeContentWebpages) {
        List<Webpage> activeChildrenTree =
            cudamiWebpagesClient.getActiveChildrenTree(activeContentWebpage.getUuid());
        activeContentWebpage.setChildren(activeChildrenTree);
      }
      return activeContentWebpages;
    } catch (HttpException ex) {
      LOGGER.warn("Could not fetch content webpages due to exc={}", ex);
      return null;
    }
  }

  public List<Webpage> getFooterPages() {
    if (cudamiConfig.getWebpage("footer") != null) {
      Pair<Webpage, Locale> webpagePair = getWebpage(cudamiConfig.getWebpage("footer"));
      if (webpagePair != null) {
        Webpage webpage = webpagePair.getLeft();
        return webpage.getChildren();
      }
    }
    return null;
  }

  private Locale getLocale(Webpage webpage) {
    if (webpage == null) {
      return null;
    }
    return webpage.getLabel().getLocales().iterator().next();
  }

  public Webpage getStartPage() {
    if (cudamiConfig.getWebpage("content") != null) {
      Pair<Webpage, Locale> webpagePair = getWebpage(cudamiConfig.getWebpage("content"));
      Webpage webpage = webpagePair.getLeft();
      return webpage;
    }
    return null;
  }

  public Pair<Webpage, Locale> getWebpage(UUID uuid) {
    Webpage webpage;
    Locale webpageLocale;
    try {
      Locale locale = Locale.forLanguageTag(LocaleContextHolder.getLocale().getLanguage());
      webpage = cudamiWebpagesClient.findActiveOne(uuid, locale);
    } catch (HttpException ex) {
      LOGGER.warn("Could not fetch webpage with uuid={} due to exc={}", uuid.toString(), ex);
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
      LOGGER.error("Website with UUID {} can not be loaded due to exc={}", websiteUuid, ex);
      return null;
    }
  }

  public boolean isActive(Webpage f) {
    LocalDate now = LocalDate.now();
    return (f.getPublicationStart() != null && f.getPublicationStart().compareTo(now) <= 0)
        && (f.getPublicationEnd() == null || f.getPublicationEnd().compareTo(now) > 0);
  }
}
