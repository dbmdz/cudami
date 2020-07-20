package de.digitalcollections.cudami.client.exceptions;

import de.digitalcollections.cudami.client.exceptions.client.ForbiddenException;
import de.digitalcollections.cudami.client.exceptions.client.HttpClientException;
import de.digitalcollections.cudami.client.exceptions.client.ImATeapotException;
import de.digitalcollections.cudami.client.exceptions.client.ResourceException;
import de.digitalcollections.cudami.client.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.cudami.client.exceptions.client.UnauthorizedException;
import de.digitalcollections.cudami.client.exceptions.client.UnavailableForLegalReasonsException;
import de.digitalcollections.cudami.client.exceptions.server.BadGatewayException;
import de.digitalcollections.cudami.client.exceptions.server.GatewayTimeOutException;
import de.digitalcollections.cudami.client.exceptions.server.HttpServerException;
import de.digitalcollections.cudami.client.exceptions.server.HttpVersionNotSupportedException;
import de.digitalcollections.cudami.client.exceptions.server.NotImplementedException;
import de.digitalcollections.cudami.client.exceptions.server.ServiceUnavailableException;

public class CudamiRestErrorDecoder {

  public static HttpException decode(String methodKey, int statusCode) {
    if (400 <= statusCode && statusCode < 500) {
      return clientException(methodKey, statusCode);
    } else if (500 <= statusCode && statusCode < 600) {
      return serverException(methodKey, statusCode);
    } else {
      return genericHttpException(methodKey, statusCode);
    }
  }

  private static HttpException clientException(String methodKey, int statusCode) {
    switch (statusCode) {
      case 401:
        return new UnauthorizedException(methodKey, statusCode);
      case 403:
        return new ForbiddenException(methodKey, statusCode);
      case 404:
        return new ResourceNotFoundException(methodKey, statusCode);
      case 413:
        return new ImATeapotException(methodKey, statusCode);
      case 422:
        return new ResourceException(methodKey, statusCode);
      case 451:
        return new UnavailableForLegalReasonsException(methodKey, statusCode);
      default:
        return new HttpClientException(methodKey, statusCode);
    }
  }

  private static HttpException genericHttpException(String methodKey, int statusCode) {
    return new HttpException(methodKey, statusCode);
  }

  private static HttpServerException serverException(String methodKey, int statusCode) {
    switch (statusCode) {
      case 501:
        return new NotImplementedException(methodKey, statusCode);
      case 502:
        return new BadGatewayException(methodKey, statusCode);
      case 503:
        return new ServiceUnavailableException(methodKey, statusCode);
      case 504:
        return new GatewayTimeOutException(methodKey, statusCode);
      case 505:
        return new HttpVersionNotSupportedException(methodKey, statusCode);
      default:
        return new HttpServerException(methodKey, statusCode);
    }
  }
}
