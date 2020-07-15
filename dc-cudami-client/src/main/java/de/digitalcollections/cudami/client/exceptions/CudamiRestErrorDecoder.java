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
import feign.Response;
import feign.codec.ErrorDecoder;

public class CudamiRestErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    if (400 <= status && status < 500) {
      return clientException(methodKey, response);
    } else if (500 <= status && status < 600) {
      return serverException(methodKey, response);
    } else {
      return genericHttpException(methodKey, response);
    }
  }

  private Exception clientException(String methodKey, Response response) {
    final int status = response.status();
    switch (status) {
      case 401:
        return new UnauthorizedException(methodKey, response);
      case 403:
        return new ForbiddenException(methodKey, response);
      case 404:
        return new ResourceNotFoundException(methodKey, response);
      case 413:
        return new ImATeapotException(methodKey, response);
      case 422:
        return new ResourceException(methodKey, response);
      case 451:
        return new UnavailableForLegalReasonsException(methodKey, response);
      default:
        return new HttpClientException(methodKey, response);
    }
  }

  private Exception genericHttpException(String methodKey, Response response) {
    return new HttpException(methodKey, response.status());
  }

  private HttpServerException serverException(String methodKey, Response response) {
    final int status = response.status();
    switch (status) {
      case 501:
        return new NotImplementedException(methodKey, response);
      case 502:
        return new BadGatewayException(methodKey, response);
      case 503:
        return new ServiceUnavailableException(methodKey, response);
      case 504:
        return new GatewayTimeOutException(methodKey, response);
      case 505:
        return new HttpVersionNotSupportedException(methodKey, response);
      default:
        return new HttpServerException(methodKey, response);
    }
  }
}
