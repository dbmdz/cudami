package de.digitalcollections.model.exception.http;

import de.digitalcollections.model.exception.Problem;
import de.digitalcollections.model.exception.http.client.ForbiddenException;
import de.digitalcollections.model.exception.http.client.HttpClientException;
import de.digitalcollections.model.exception.http.client.ImATeapotException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.exception.http.client.UnauthorizedException;
import de.digitalcollections.model.exception.http.client.UnavailableForLegalReasonsException;
import de.digitalcollections.model.exception.http.client.UnprocessableEntityException;
import de.digitalcollections.model.exception.http.server.BadGatewayException;
import de.digitalcollections.model.exception.http.server.GatewayTimeOutException;
import de.digitalcollections.model.exception.http.server.HttpServerException;
import de.digitalcollections.model.exception.http.server.HttpVersionNotSupportedException;
import de.digitalcollections.model.exception.http.server.NotImplementedException;
import de.digitalcollections.model.exception.http.server.ServiceUnavailableException;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.net.MalformedURLException;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpErrorDecoder {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpErrorDecoder.class);

  private static HttpException clientException(
      String methodKey, int statusCode, String requestUrl) {
    switch (statusCode) {
      case 401:
        return new UnauthorizedException(methodKey, statusCode, requestUrl);
      case 403:
        return new ForbiddenException(methodKey, statusCode, requestUrl);
      case 404:
        return new ResourceNotFoundException(methodKey, statusCode, requestUrl);
      case 418:
        return new ImATeapotException(methodKey, statusCode, requestUrl);
      case 422:
        return new UnprocessableEntityException(methodKey, statusCode, requestUrl);
      case 451:
        return new UnavailableForLegalReasonsException(methodKey, statusCode, requestUrl);
      default:
        return new HttpClientException(methodKey, statusCode, requestUrl);
    }
  }

  public static HttpException decode(String methodKey, int statusCode, HttpResponse response) {
    String requestUrl = null;
    try {
      if (response != null) {
        requestUrl = response.request().uri().toURL().toString();
      }
    } catch (MalformedURLException ex) {
      LOGGER.warn("Invalid request Url for: " + response.request().uri());
    }

    final byte[] body = (byte[]) response.body();
    if (body != null && body.length > 0) {
      try {
        Problem problem =
            new DigitalCollectionsObjectMapper().readerFor(Problem.class).readValue(body);
        LOGGER.error("Got problem=" + problem);
      } catch (Exception e) {
        LOGGER.error("Got response=" + new String(body) + " but cannot construct problem: " + e, e);
      }
    }

    if (400 <= statusCode && statusCode < 500) {
      return clientException(methodKey, statusCode, requestUrl);
    } else if (500 <= statusCode && statusCode < 600) {
      return serverException(methodKey, statusCode, requestUrl);
    } else {
      return genericHttpException(methodKey, statusCode, requestUrl);
    }
  }

  private static HttpException genericHttpException(
      String methodKey, int statusCode, String requestUrl) {
    return new HttpException(methodKey, statusCode, requestUrl);
  }

  private static HttpServerException serverException(
      String methodKey, int statusCode, String requestUrl) {
    switch (statusCode) {
      case 501:
        return new NotImplementedException(methodKey, statusCode, requestUrl);
      case 502:
        return new BadGatewayException(methodKey, statusCode, requestUrl);
      case 503:
        return new ServiceUnavailableException(methodKey, statusCode, requestUrl);
      case 504:
        return new GatewayTimeOutException(methodKey, statusCode, requestUrl);
      case 505:
        return new HttpVersionNotSupportedException(methodKey, statusCode, requestUrl);
      default:
        return new HttpServerException(methodKey, statusCode, requestUrl);
    }
  }
}
