package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.client.backend.api.repository.WebsiteRepository;
import de.digitalcollections.cudami.model.impl.entity.WebsiteImpl;
import feign.Feign;
import feign.gson.GsonDecoder;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl implements WebsiteRepository<WebsiteImpl, Long> {

  private final WebsiteRepositoryEndpoint endpoint = Feign.builder()
          .decoder(new GsonDecoder())
          .target(WebsiteRepositoryEndpoint.class, "http://localhost:8080");

  @Override
  public WebsiteImpl create() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<WebsiteImpl> find(PageRequest pageRequest) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<WebsiteImpl> findAll() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public WebsiteImpl findOne(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public WebsiteImpl save(WebsiteImpl website) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
