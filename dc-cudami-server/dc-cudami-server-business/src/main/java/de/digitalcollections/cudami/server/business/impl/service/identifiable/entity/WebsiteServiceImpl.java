package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for Website handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class WebsiteServiceImpl extends EntityServiceImpl<Website> implements WebsiteService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteServiceImpl.class);

  private final WebpageService webpageService;

  public WebsiteServiceImpl(
      WebsiteRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      CudamiConfig cudamiConfig,
      LocaleService localeService,
      WebpageService webpageService) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
    this.webpageService = webpageService;
  }

  @Override
  public PageResponse<Webpage> findRootWebpages(UUID uuid, PageRequest pageRequest) {
    return webpageService.findRootWebpagesForWebsite(uuid, pageRequest);
  }

  @Override
  public List<Webpage> getRootWebpages(UUID uuid) {
    List<Webpage> rootWebpages = ((WebsiteRepository) repository).findRootWebpages(uuid);
    webpageService.setPublicationStatus(rootWebpages);
    return rootWebpages;
  }

  @Override
  public boolean updateRootWebpagesOrder(Website website, List<Webpage> rootPages) {
    return ((WebsiteRepository) repository).updateRootWebpagesOrder(website, rootPages);
  }

  @Override
  public boolean updateRootWebpagesOrder(UUID websiteUuid, List<Webpage> rootPages) {
    return ((WebsiteRepository) repository).updateRootWebpagesOrder(websiteUuid, rootPages);
  }
}
