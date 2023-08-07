package de.digitalcollections.cudami.client.semantic;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.list.buckets.Bucket;
import de.digitalcollections.model.list.buckets.BucketObjectsRequest;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.semantic.Headword;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CudamiHeadwordsClient extends CudamiRestClient<Headword> {

  public CudamiHeadwordsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Headword.class, mapper, API_VERSION_PREFIX + "/headwords");
  }

  public BucketObjectsResponse<Headword> findBucketObjects(
      BucketObjectsRequest<Headword> bucketObjectsRequest) throws TechnicalException {
    BucketObjectsResponse<Headword> result;
    String url = String.format("%s/bucketobjects", baseEndpoint);
    Bucket<Headword> bucket = bucketObjectsRequest.getBucket();
    if (bucket != null && bucket.getStartObject() != null && bucket.getEndObject() != null) {
      Headword startObject = bucket.getStartObject();
      Headword endObject = bucket.getEndObject();
      url = url + "?startId=" + startObject.getUuid();
      url = url + "&endId=" + endObject.getUuid();

      url = url + "&" + getFindParamsAsString(bucketObjectsRequest);

      result =
          (BucketObjectsResponse<Headword>) doGetRequestForObject(url, BucketObjectsResponse.class);
    } else {
      result = new BucketObjectsResponse<>(bucketObjectsRequest, new ArrayList<>());
    }
    return result;
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

    url = url + "&" + getSortParams(bucketsRequest);

    BucketsResponse<Headword> result =
        (BucketsResponse<Headword>) doGetRequestForObject(url, BucketsResponse.class);
    return result;
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
