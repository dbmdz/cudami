package de.digitalcollections.cudami.server.assertj;

import org.apache.http.entity.ContentType;
import org.assertj.core.api.AbstractAssert;
import org.springframework.http.HttpHeaders;

public class HttpHeadersAssert extends AbstractAssert<HttpHeadersAssert, HttpHeaders> {

  private final HttpHeaders actualHttpHeaders;

  protected HttpHeadersAssert(HttpHeaders actualHttpHeaders) {
    super(actualHttpHeaders, HttpHeadersAssert.class);
    this.actualHttpHeaders = actualHttpHeaders;
  }

  /**
   * This check only cares about the mime type, not the charset
   */
  public void hasContentType(ContentType expectedContentType)  {
    if ((actualHttpHeaders == null || actualHttpHeaders.getContentType() == null) && expectedContentType != null) {
      failWithMessage("Expected non null content type");
    }
    if (actualHttpHeaders.getContentType() != null && expectedContentType == null) {
      failWithMessage("Expected null content type");
    }

    if (!actualHttpHeaders.getContentType().toString().equals(expectedContentType.getMimeType())) {
      failWithMessage("Different content types. Expected=" + expectedContentType.getMimeType()
          + ", actual=" + actualHttpHeaders.getContentType().toString());
    }
  }
}
