package de.digitalcollections.cudami.client.semantic;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.HttpErrorDecoder;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.list.buckets.Bucket;
import de.digitalcollections.model.list.buckets.BucketObjectsRequest;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.semantic.Headword;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class CudamiHeadwordsClient extends CudamiRestClient<Headword> {

  public CudamiHeadwordsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Headword.class, mapper, API_VERSION_PREFIX + "/headwords");
  }

  public BucketObjectsResponse<Headword> findBucketObjects(
      BucketObjectsRequest<Headword> bucketObjectsRequest) throws TechnicalException {
    String url = String.format("%s/bucketobjects", baseEndpoint);
    Bucket<Headword> bucket = bucketObjectsRequest.getBucket();
    Headword startObject = bucket.getStartObject();
    Headword endObject = bucket.getEndObject();
    url = url + "?startId=" + startObject.getUuid();
    url = url + "&endId=" + endObject.getUuid();

    int pageNumber = bucketObjectsRequest.getPageNumber();
    if (pageNumber >= 0) {
      url = url + "&pageNumber=" + pageNumber;
    }
    int pageSize = bucketObjectsRequest.getPageSize();
    if (pageSize > 0) {
      url = url + "&pageSize=" + pageSize;
    }

    HttpRequest req = createGetRequest(url);
    // TODO add creation of a request id if needed
    //            .header("X-Request-Id", request.getRequestId())
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("GET " + url, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      BucketObjectsResponse result = mapper.readerFor(BucketObjectsResponse.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  public BucketsResponse<Headword> findBuckets(BucketsRequest<Headword> bucketsRequest)
      throws TechnicalException {
    int numberOfBuckets = bucketsRequest.getNumberOfBuckets();
    String url = String.format("%s/buckets?numberOfBuckets=%d", baseEndpoint, numberOfBuckets);

    Bucket<Headword> parentBucket = bucketsRequest.getParentBucket();
    if (parentBucket != null) {
      Headword startObject = parentBucket.getStartObject();
      Headword endObject = parentBucket.getEndObject();
      url = url + "&startId=" + startObject.getUuid();
      url = url + "&endId=" + endObject.getUuid();
    }

    HttpRequest req = createGetRequest(url);
    try {
      HttpResponse<byte[]> response = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      Integer statusCode = response.statusCode();
      if (statusCode >= 400) {
        throw HttpErrorDecoder.decode("GET " + url, statusCode, response);
      }
      // This is the most performant approach for Jackson
      final byte[] body = response.body();
      if (body == null || body.length == 0) {
        return null;
      }
      BucketsResponse result = mapper.readerFor(BucketsResponse.class).readValue(body);
      return result;
    } catch (IOException | InterruptedException e) {
      throw new TechnicalException("Failed to retrieve response due to connection error", e);
    }
  }

  public List getRandomHeadwords(int count) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/random?count=%d", baseEndpoint, count), Headword.class);
  }

  public List getRelatedArticles(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/related/articles", baseEndpoint, uuid), Article.class);
  }
}
