package de.digitalcollections.cudami.client.semantic;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.semantic.Headword;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiHeadwordsClient extends CudamiBaseClient<Headword> {
  public CudamiHeadwordsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Headword.class, mapper);
  }

  public Headword create() {
    return new Headword();
  }

  public PageResponse<Headword> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/headwords", pageRequest);
  }

  public SearchPageResponse<Headword> find(SearchPageRequest pageRequest) throws HttpException {
    return this.doGetSearchRequestForPagedObjectList("/v5/headwords", pageRequest);
  }

  public Headword findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/headwords/%s", uuid));
  }

  public List getRelatedArticles(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/headwords/%s/related/articles", uuid), Article.class);
  }

  public List getRelatedHeadwordEntries(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/headwords/%s/related/headwordentries", uuid), HeadwordEntry.class);
  }

  public Headword save(Headword headword) throws HttpException {
    return doPostRequestForObject("/v5/headwords", headword);
  }

  public Headword update(UUID uuid, Headword headword) throws HttpException {
    return doPutRequestForObject(String.format("/v5/headwords/%s", uuid), headword);
  }
}
